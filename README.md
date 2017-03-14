my-health-partner-android
=========================
Description :
-------------
*************

The final application will inform users their daily follow-up for each activity (walking or running). In fact, the appication send accelerometer & profile data to the server. The server answers by returning the type of activity he recognized with the classification algorithm.

### Profile

The user is able to inform his profile information (gender,height, weight, birthday). It will help the algorithm to improve the follow-up by adapting calculations according to this profile.

### Follow-up

Each day, the user will be able to see the steps done and the burned calories on the main activity. A pie chart is also set to view the number of steps done compared to a goal number of steps.

Deployement :
-------------
*************

The application will work from Android Studio 4.4 (KitKat)

To get the project, create a new Android Studio Project :
File -> New -> Project From Version Control -> GitHub
Here is the Git Repository URL : https://github.com/Serli/my-health-partner-android.git

Developer help :
-------------
*************

To use your own server, just change the Retrofit baseUrl attribute in the sendAcquisition method of the MainController. 

For example : retrofit.baseUrl(String.valueOf("Your URL"))





