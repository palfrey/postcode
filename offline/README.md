# Offline Postcode database by moosd
## Thanks to:
Ordanance Survey - https://www.ordnancesurvey.co.uk/business-and-government/products/os-open-names.html
Dr Fry - http://www.hannahfry.co.uk/blog/2012/02/01/converting-british-national-grid-to-latitude-and-longitude-ii

## What to do
If you want to update (i.e. destroy and rebuild the postcode database) based on newer data, just download all of the OS OpenNames CSV files into a folder and run `./mkdb.py <foldername>`

For example,
```
./mkdb.py Data/CSV
```

And once it has finished, you will have a postcodes.db :)

This can then be used by the postcodes app to read off your postcode without an internet connection.
