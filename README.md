# myyandextranslate
test project for mobilization
@author: SemenNag

Application Description
A simple android application, which contains:
a) retreive supporting by Yandex Переводчик languages
b) translates user input texts according to selected langs
c) saves the history and enables to mark as favorites
d) allows user to look through translation history even without inet connection

Technical Review
User actions in main translate activity, calls for rest services. Application is build with DataDroid module, which enables 
to make api calls in separate thread. The result of network response handles and stores in local db by ContentProvider.
ContentProvider notify data observes, such as CursorLoaders, which retreive data to client.
