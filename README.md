agendawatchfacePluginAPI
========================

The API to create plugins for AgendaWatchface (Android).

To use it, simply import this project into your (Eclipse) workspace via File -> Import... -> Existing Android Code into Workspace...
Follow the comments/javadoc in AgendaWatchfacePlugin.java.

Short version: 
- inherit from AgendaWatchfacePlugin, which is a BroadcastReceiver.
- register this new class as a BroadcastReceiver in the manifest and also add an intent filter for intent action de.janbo.agendawatchface.intent.action.provider to it.
- implement the abstract methods/callbacks. [If you do heavy lifting or need to use a ContentObserver, you may want to write a Service and delegate some tasks to it]
- call publishData() with a list of AgendaItems to make them accessible to the AgendaWatchfaceService and automatically publish them to the watch

You may consider the following example code (available on GitHub on my account):
- Any.do plugin. This is quite simple, mostly because Any.do does a Broadcast to notify for changes. So basically, only the AnyDoProvider class is needed.
- Calendar plugin (bundled in the main project). Uses the "delegate to service" approach so that it can watch for changes in the calendar via a ContentObserver.
- Notification plugin. Also uses a Service to watch for new notifications.

If you encounter any problems, don't hesitate to contact me.