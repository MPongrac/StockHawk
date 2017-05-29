# Stock Hawk

This began as the starter code for project 3 in Udacity's [Android Developer Nanodegree]
(https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801). 

Check out Udacity's [Advanced Android App Development](https://www.udacity.com/course/advanced-android-app-development--ud855) course to get the skills you'll need to complete this project!

If you're a Nanodegree student check your work againt the project [rubric](https://review.udacity.com/#!/rubrics/140/view), then click [here](https://classroom.udacity.com/nanodegrees/nd801/parts/8011345406/project) to submit your project.

# Student Notes - Michael Pongrac AKA AppMakerMike

When this app was successfully tested during the Hackathon on the 13th and 14th of May 
2017, the YahooFinance API was delivering historical quote information.  I had posted a
screenshot of the graph functionality as I implemented it, since it was so different 
from most.  This screenshot has also been included as proof that it works when data is
provided.  I chose not to use dummy data to illustrate the functionality in an effort
to show that my version of the app, does what it should when there is no available 
data.

One important note, is that since the API has ceased to deliver historical quote 
details, the app takes longer to display the list of current details and the little 
"hour glass" does not appear as it did in the past.

The widget scrolls automatically through the list of stocks.  When the data source has 
been fully updated, the widget is also automatically reloaded.  The user may manually 
scroll in either direction to review the individual stock values.  When the user clicks
on a specific stock item, the appropriate graph is displayed.

When the user is in the stock list of the app, they can use a swipe action to delete an
existing stock from the list.

## Update: 2017-05-29 

With release 3.7.0, the YahooFinance API is, once again, delivering historical quote 
details.

All requirements should now be fulfilled. 