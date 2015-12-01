# Offline Postcode database by moosd
## Thanks to:
Ordanance Survey - https://www.ordnancesurvey.co.uk/business-and-government/products/os-open-names.html
Dr Fry - http://www.hannahfry.co.uk/blog/2012/02/01/converting-british-national-grid-to-latitude-and-longitude-ii

## How to use it
Install the postcode.apk and copy postcodes.db to /sdcard/postcodes.db

You can find both of these files in releases.

## How to recreate your own database
This bit assumes you are running some variant of Linux. You may need to adapt it if you are using Windows or Mac OSX.

You will first need to fetch the data. Go to https://www.ordnancesurvey.co.uk/opendatadownload/products.html and scroll down to `OS Open Names` and make sure "Supply format" is CSV. Then Shift+Select all the items in the list from HP to TV to get all the data.

You will get an email after filling the form out. Copy the contents of the email into a file `list` in this folder and fetch all the links into a folder.

```
mkdir Src
cd Src
cat ../list | sed 's/http/\nhttp/g' | grep "http" | grep "zip" | while read u; do wget "$u"; done
```

You will now have a folder `Src` full of zip files with awkward filenames. Extract them like so:

```
for f in *; do unzip -u "$f"; done
cd ..
```

And this will dump a `DATA` folder into the `Src` folder, while taking you back up a level to this folder.

To process this into a useful database, just run the following:

```
./mkdb.py Src/DATA
```

And once it has finished, you will have a postcodes.db :) Put this into /sdcard/postcodes.db and the offline backend should pick it up.

This can then be used by the postcodes app to read off your postcode without an internet connection.
