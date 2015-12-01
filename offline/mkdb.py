#!/usr/bin/env python3

import sqlite3
import sys, os
import csv, glob
from math import sqrt, pi, sin, cos, tan, atan2 as arctan2

# from http://www.hannahfry.co.uk/blog/2012/02/01/converting-british-national-grid-to-latitude-and-longitude-ii
# Thanks Dr Fry! *avoids learning about map projections again*
def OSGB36toWGS84(E,N):
    #E, N are the British national grid coordinates - eastings and northings
    a, b = 6377563.396, 6356256.909     #The Airy 180 semi-major and semi-minor axes used for OSGB36 (m)
    F0 = 0.9996012717                   #scale factor on the central meridian
    lat0 = 49*pi/180                    #Latitude of true origin (radians)
    lon0 = -2*pi/180                    #Longtitude of true origin and central meridian (radians)
    N0, E0 = -100000, 400000            #Northing & easting of true origin (m)
    e2 = 1 - (b*b)/(a*a)                #eccentricity squared
    n = (a-b)/(a+b)

    lat,M = lat0, 0

    while N-N0-M >= 0.00001:
        lat = (N-N0-M)/(a*F0) + lat;
        M1 = (1 + n + (5./4)*n**2 + (5./4)*n**3) * (lat-lat0)
        M2 = (3*n + 3*n**2 + (21./8)*n**3) * sin(lat-lat0) * cos(lat+lat0)
        M3 = ((15./8)*n**2 + (15./8)*n**3) * sin(2*(lat-lat0)) * cos(2*(lat+lat0))
        M4 = (35./24)*n**3 * sin(3*(lat-lat0)) * cos(3*(lat+lat0))
        M = b * F0 * (M1 - M2 + M3 - M4)

    nu = a*F0/sqrt(1-e2*sin(lat)**2)

    rho = a*F0*(1-e2)*(1-e2*sin(lat)**2)**(-1.5)
    eta2 = nu/rho-1

    secLat = 1./cos(lat)
    VII = tan(lat)/(2*rho*nu)
    VIII = tan(lat)/(24*rho*nu**3)*(5+3*tan(lat)**2+eta2-9*tan(lat)**2*eta2)
    IX = tan(lat)/(720*rho*nu**5)*(61+90*tan(lat)**2+45*tan(lat)**4)
    X = secLat/nu
    XI = secLat/(6*nu**3)*(nu/rho+2*tan(lat)**2)
    XII = secLat/(120*nu**5)*(5+28*tan(lat)**2+24*tan(lat)**4)
    XIIA = secLat/(5040*nu**7)*(61+662*tan(lat)**2+1320*tan(lat)**4+720*tan(lat)**6)
    dE = E-E0

    lat_1 = lat - VII*dE**2 + VIII*dE**4 - IX*dE**6
    lon_1 = lon0 + X*dE - XI*dE**3 + XII*dE**5 - XIIA*dE**7

    H = 0
    x_1 = (nu/F0 + H)*cos(lat_1)*cos(lon_1)
    y_1 = (nu/F0+ H)*cos(lat_1)*sin(lon_1)
    z_1 = ((1-e2)*nu/F0 +H)*sin(lat_1)

    s = -20.4894*10**-6
    tx, ty, tz = 446.448, -125.157, + 542.060
    rxs,rys,rzs = 0.1502,  0.2470,  0.8421
    rx, ry, rz = rxs*pi/(180*3600.), rys*pi/(180*3600.), rzs*pi/(180*3600.)
    x_2 = tx + (1+s)*x_1 + (-rz)*y_1 + (ry)*z_1
    y_2 = ty + (rz)*x_1  + (1+s)*y_1 + (-rx)*z_1
    z_2 = tz + (-ry)*x_1 + (rx)*y_1 +  (1+s)*z_1

    a_2, b_2 =6378137.000, 6356752.3141
    e2_2 = 1- (b_2*b_2)/(a_2*a_2)
    p = sqrt(x_2**2 + y_2**2)

    lat = arctan2(z_2,(p*(1-e2_2)))
    latold = 2*pi
    while abs(lat - latold)>10**-16:
        lat, latold = latold, lat
        nu_2 = a_2/sqrt(1-e2_2*sin(latold)**2)
        lat = arctan2(z_2+e2_2*nu_2*sin(latold), p)

    lon = arctan2(y_2,x_2)
    H = p/cos(lat) - nu_2

    lat = lat*180/pi
    lon = lon*180/pi

    return lat, lon

# Right, now loop through dataset from OS and turn it into something useful for postcodes
if len(sys.argv) < 2:
    print("Please pass me a folder of csv files to parse")
    sys.exit(1)

con = None
dbname = "postcodes.db"

try:
    os.remove(dbname)
except:
    pass

try:
    con = sqlite3.connect(dbname)
    cur = con.cursor()
    cur.execute('CREATE TABLE codes(postcode VARCHAR, lat VARCHAR, lon VARCHAR)')

    for f in [os.path.join(sys.argv[1],fn) for fn in next(os.walk(sys.argv[1]))[2]]:
        infile = csv.reader(open(f, 'rU'), delimiter = ',')
        for _,_,Code,_,_,_,_,Type,E,N,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_ in infile:
            if Type == "Postcode":
                lat, lon = OSGB36toWGS84(float(E), float(N))
                #print([str(lat), str(lon), str(Code)])
                cur.execute('INSERT INTO codes(postcode,lat,lon) VALUES(?, ?, ?)', (Code, lat, lon))

    con.commit()
    cur.execute('SELECT Count(*) FROM codes')
    data = cur.fetchone()
    print("Inserted: %s entries" % data[0])
except sqlite3.Error as e:
    print("Error %s:" % e.args[0])
    sys.exit(1)
finally:
    if con:
        con.close()
