# shoot
a simple game with the same basic idea as moorhuhn

-> shoot down the bills to get no penalty points

-> hit the coins to refill your account

-> for more freedom let the bomb explode

# VR-Version

There is also a jMonkey based virtual reality version out now (actually it is still in development ;P).
You can start it by running the main()-methode in class InvoiceShootVR.

You need following libraries in your classpath:
- jMonkeyEngine (tested with version 3.2.1 stable)
- Google gson (tested with version 2.8.5)
- lwjgl (tested with version 3.2.0)
- jna (tested with version 5.1.0)

Notice that you need to compile the jMonkeyEngine by your own, to get the binaries of the jme3-vr module (this module is not contained in any current released binary packages). Just download the engine from https://github.com/jMonkeyEngine/jmonkeyengine und call gradle build inside the main folder.

In class Constants you can switch between OpenVR or native Oculus support. Just comment/uncomment the corresponding constants (it is explained in code).
