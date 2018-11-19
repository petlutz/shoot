# shoot
a simple game with the same basic idea as moorhuhn

-> shoot down the bills to get no penalty points

-> hit the coins to refill your account

-> for more freedom let the bomb explode

# VR version

There is also a jmonkey based virtual reality version out now (still in development).
You can start it by running the main()-methode in class InvoiceShootVR.

You need following libraries in your classpath:
- jMonkeyEngine (tested with version 3.2.1 stable)
- Google gson (tested with version 2.8.5)
- lwjgl (tested with 3.2.0)
- jna (tested with 5.1.0)

Notice that you need to compile the jMonkeyEngine by your own, to get the binaries of jme3-vr module (this module is not contained in any current released binary packages). Just download the engine from https://github.com/jMonkeyEngine/jmonkeyengine und call gradle build inside the main folder.

