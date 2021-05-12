

https://youtu.be/opPlUBR5UAo

To debug or compile this project, import the client directory as a Gradle project in Eclipse. Then create a run configuration with the following program arguments: `--debug --dev-assets ../assets_dev --platform opengl`. Also, set the working directory of the run configuration to either the windows or the mac or the linux directory of the client directory depending on the platform, so the program can load the correct native libraries.

The source code can be found in src/main/java. The packages in this directory are:

 - `net.lax1dude.cs50_final_project` - Utility classes, do not depend on client side resources
 - `net.lax1dude.cs50_final_project.client` - Core classes for whatever application will eventually be built on top of the rendering engine
 - `net.lax1dude.cs50_final_project.client.main` - Contains the main class and launch dialog box
 - `net.lax1dude.cs50_final_project.client.renderer` - Contains all classes required for rendering
 - `net.lax1dude.cs50_final_project.client.opengl` - Contains simple java wrapper objects for different opengl object types for convenience

To configure the renderer, check `net.lax1dude.cs50_final_project.client.GameConfiguration`. It has variables to adjust the GLSL compiler settings, shadowmap resolutions, the cloud map resolution, screen space reflection quality, the far plane, and flags for enabling and disabling ambient occlusion, volumetric sunlight, bloom, soft shadows, cloud movement, and screen space reflections.

To configure the scene, create a custom instance of RenderScene and populate the different variables with custom values. There are linked sets for adding and removing `ObjectRenderer`, `LightRenderer`, and `ShadowLightRenderer` objects to the scene.

There is a tools application for generating vertex data from wavefront obj files, generating new fonts atlases from truetype font files, and compositing textures for use with the g-buffer material system from individual texture files for every material variable. Import the tools directory as a java project into eclipse.
