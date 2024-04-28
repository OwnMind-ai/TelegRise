# TelegRise [PREVIEW]
Java framework that simplifies the process of creating Telegram bots and provides a more comfortable experience for developers.

### IMPORTANT
This project is on the developing stage and **do not** intend to be used in production. If you do so, a lot of compatibility problems may occur from version to version.

## Quickstart

Firstly, create your first transcription .xml file. Here is an example of one (index.xml):

```xml
<?xml version="1.0" encoding="UTF-8" ?>

<!--Import your tree controllers and static method references here -->
<?import org.example.telegrise.SimpleController ?>

<bot username="SimpleBot" token="bot's token">
    <head>
        <link src="..."/>  <!-- List your other transcriptions here -->
    </head>

    <menu name="RootMenu">
        <tree name="StartTree" commands="start" chats="private" 
              description="Bot start" controller="SimpleController">
            <!-- Sends a message to a chat from which '/start' command came -->
            <send>
                <text>Hi there!</text>
                <keyboard type="inline">
                    <row>
                        <button data="say-hello">Bye</button>
                    </row>
                </keyboard>
            </send>
            
            <!-- Executes if user pressed the inline button that returns a callback query
                 with 'say-hello' data (see keyboard tag above),
                 invokes "logResponse" method in SimpleController instance-->
            <branch callbackTriggers="say-hello" invoke="#logResponce">
                <edit>Bye, ${update.getCallbackQuery().getFrom().getFirstName()}</edit>
            </branch>
            
            <!-- Executes default branch if no valid branch was found and if
                 'messageSent' method in SimpleController instance returns 'true' and
                 the message text is not "Hello!"-->
            <default when="#messageSent AND #messageText(update, &quot;Hello!&quot;) -> #not">
                <send>Unrecognized command. Say what?</send>
            </default>
        </tree>
    </menu>
</bot>
```

Then, if you need to include logic to your tree, you need to create a **Tree Controller**:

```java
@TreeController
public class SimpleController {
    @Resource  // Injects resource into created instance, customizable
    private SessionMemory memory;

    @OnCreate
    public void initialize() {
        System.out.println("Someone pressed '/start'");
    }

    @Reference  // Indicates that method can be referenced in transcription by using '#' sign
    public void logResponse(Update update) {
        this.memory.put("response", update);
    }

    @Reference
    public boolean messageSent(Update update) {
        return update.hasMessage();
    }

    @Reference
    public boolean messageText(Update update, String text){
        return text.equals(update.getMessage().getText());
    }
}
```

And, to start your application, here is the main class:

```java
public class Main {
    public static void main(String[] args) {
        TelegRiseApplication application = new TelegRiseApplication(new File("index.xml"), Main.class);
        
        // Optional:
        application.addResourceFactory(...); // Your custom resources here
        application.addService(...); // Your custom services here
        application.setRoleProvider(...);  // Your role provider
        
        application.start();
    }
}
```
