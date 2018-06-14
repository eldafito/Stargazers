# Github Stargazers

## Approach
For this project I decided to use an incremental approach as it fits well with the step specifications of the delivery.

This project is written in Java beacause because I feel more comfortable with this language, being migrated to Kotlin only a few weeks ago.

### Search users
Typing the repo owner's username freely did not seem like a good idea.
A simplified live search seemed to me the best solution and it was possible to develop it thanks to the search API provided by the Github Service.

### List user repos
Assuming that the repos list size associated to the same owner is generally a limited number, easily accessible from a simple list, I decided to use a spinner view to display them.

Obviously the repo selection will be possible only after having indicated a user and fetching related result from Github network API call.

### List Stargazers
Using the input of a user and a repo, it will be trivial to request the stargazers list from the API and show them in the associated activity reusing the previously created user adapter.

## Key notes

### Search
In order to improve performance and optimize internet traffic, I decided to delay search API calls.
This approach was very useful to avoid unnecessary calls to Github services while the user was typing.

### Pagination
By consulting the GitHub documentation, I noticed that all the calls I would use in the Stargazers app required pagination management.

Pagination was handled with an integer variable that was incremented each time it was used.
When the result of the call was successful but did not return any values, it meant that we had reached the end of the list and the *page* variable was set to the symbolic value of *null*.

Depending on the context, the request for new data from the server is sent by the user's scroll to the last element of the list or directly by the response of the previous call.

## Installation
Standard Android Studio 3.1.3 project.
Sync with gradle 4.4, build, run and enjoy :)

Demo video of Stargazers app can be found on root directory. 
