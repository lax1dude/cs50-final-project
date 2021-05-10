# Design Document
The goal of this project was to implement a PBR rendering engine using Java and OpenGL. To create this application I used the LWJGL native OpenGL ES bindings for Java to interface with Google's ANGLE OpenGL ES implementation used in chrome as the backend for WebGL. The reason I chose Java and ANGLE as my development environment is for the universal compatibility of the Java language and the OpenGL ES API. Every major platform supports both in some way so this rendering engine can be repurposed at some point for mobile development or even console development.

My source code repository is available here: [https://github.com/LAX1DUDE/cs50-final-project](https://github.com/LAX1DUDE/cs50-final-project)

If the pictures do not load you can access a copy of this doc here: [https://github.com/LAX1DUDE/cs50-final-project/blob/master/DESIGN.md]

## How a frame is rendered
I will be using a program called [RenderDoc](https://renderdoc.org/) to display real screenshots of every render target used in the game while it's running. RenderDoc injects a layer between the Java code and OpenGL so it can intercept draw calls and display debug information related to them in a GUI.

![enter image description here](https://i.gyazo.com/eae78c24379653a53dcefb49e0cf8ced.png)

**Cloud Map Update**

The first thing the game does is update a single tile of a paraboloid map containing the cloud information for rendering the PBR clouds in the sky. The tile is created by raytracing a GPU accelerated Worley Noise algorithm. A seam is visible in the screenshot because the clouds are moving and the tiles have only partially been updated. The clouds are triple buffered so the actual cloud maps used by the sky mesh is only updated once every tile in the back buffer has received an update, and then the two front buffers are crossfaded by the pixel shader

![cloud paraboloid map](https://i.gyazo.com/bdf0be342e3edf819dd61e096af95dbc.png)

**Depth Prepass**

Next, all solid geometry is rendered to a depth buffer, outputting the distance each pixel is from the viewer. This is done for optimization so the depth test mode can be set to GL_EQUAL when the main color pass is performed to avoid rendering obstructed geometry. The depth buffer is reversed (0 is far, 1 is near) because floating point integers have more precision when closer to 0, which greatly reduces Z-fighting for distant geometry. A stencil buffer is also generated, acting as a mask for which pixels will be filled with sky later.

![depth prepass](https://i.gyazo.com/9eeff3ddd211eb23b89d5c7ba712bdc5.png)

**Geometry Buffer Generation**

My engine uses deferred rendering technique rendering to three render targets simultaneously, a target storing each pixel's diffuse color, a target storing each pixel's normal vector (the direction it faces), and a target storing the material properties of each pixel (as RGBA, representing the material metalness, roughness, specular, ssr). These are generated in a single pass

![enter image description here](https://i.gyazo.com/27895c9b10fe6949e7a3ad001a90f15a.png)
![enter image description here](https://i.gyazo.com/bdda9d3dfc053b5efc60886fb93ce5e8.png)
![enter image description here](https://i.gyazo.com/32ec3ca175439c76e972f5471288c039.png)

An example of obstructed geometry detected through the depth prepass can be seen here, while the rendering pass has only partially completed:

![enter image description here](https://i.gyazo.com/0e028558b1b2fcc826fe53efe5e1ff29.png)

**Shadow Map Generation**

Next, the scene is rendered four times from the perspective of the sun to a depth buffer. This is used later to calculate which pixels are obstructed by shadows from the sun. It is rendered from progressively farther distances from the flycam to maintain shadow quality at short distances and long distances simultaneously

![enter image description here](https://i.gyazo.com/5c809cad0397c7dff7ca076de5d438c1.png)

Then shadow maps are rendered for every shadow casting light in the scene to a tile in a large depth buffer atlas

![enter image description here](https://i.gyazo.com/cbc7da83d2a900c07be367d5bf9eab16.png)

**Sun Shadow Calculation**

This step uses the four depth buffers rendered from the perspective of the sun to calculate which pixels are obstructed by a shadow

![enter image description here](https://i.gyazo.com/df3500e5e8d547fb5e84a0352c404944.png)

**Cube Map Generation**

To calculate reflections later, the scene is rendered six more times to six faces of a cube, using forward rendering. The shadow maps and cloud maps are used to render shadows and the sky and sun in the background. The render target uses linear color space stored as 16-bit floats

![enter image description here](https://i.gyazo.com/5744b185e77d082e26605946cce242f7.png)
![enter image description here](https://i.gyazo.com/759f4c536469e44f577bbdcdb5e3939f.png)
![enter image description here](https://i.gyazo.com/0095c7e37ad8fca2de0a2c00e66d8d00.png)
![enter image description here](https://i.gyazo.com/15138202f8d1677020ed6ed9104ab329.png)
![enter image description here](https://i.gyazo.com/718ab689749cb5cac4109d57b86a0a87.png)
![enter image description here](https://i.gyazo.com/32b7e6bcd5936eda993e7e7430457bb4.png)

**Dual Paraboloid Generation**

The cubemap is converted to a dual paraboloid map and then it is repeatedly blurred and blitted to a texture atlas for later use when compositing materials

![enter image description here](https://i.gyazo.com/d837748cfa1c04efa001ea096a74c4aa.png)
![enter image description here](https://i.gyazo.com/aac6dcdc8b9969919a44beae2c3369df.png)

**Irradiance Map**

The irradiance map is recalculated once per second as it is a very compute-intensive process if PBR is the priority. It is used to calculate global ambient illumination

![enter image description here](https://i.gyazo.com/093a82e1b18fe2dce9000c552fb66695.png)

**Screen Space Ambient Occlusion**

Ambient occlusion is calculated at half resolution, at first a noisy result is produced but it is then blurred by a two pass depth-aware shader to produce a clean result

![enter image description here](https://i.gyazo.com/c170fa00b82497c7b0235ff77e2a7b7f.png)![enter image description here](https://i.gyazo.com/4250b143b14fa920c01eac4aa1b53b6e.png)
![enter image description here](https://i.gyazo.com/a652d237e3c391994ca9e37b94193226.png)![enter image description here](https://i.gyazo.com/4c4e47b1cbbebfc91a792b8007e05ebe.png)

**Light source calculations**

The blend mode is set to GL_ONE + GL_ONE (Add every rendered pixel's values to the value of the pixel already in the buffer) and then every light source's diffuse and specular component is calculated and rendered to the buffer sequentially, calculating the final lighting value of every pixel 

![enter image description here](https://i.gyazo.com/b7878cbb533bdd6b4a2d7d82c9c421fc.png)
![enter image description here](https://i.gyazo.com/4b0cfbfd98397aaa8cf8e6bff014ae21.png)

**Screen Space Reflections**

The previous frame's color and depth buffer is raytraced and screen space reflection values are calculated for every pixel

![enter image description here](https://i.gyazo.com/38faaace9f7987bf269426407c42da33.png)

This is done at half resolution for optimization

**Buffer Combination**

The diffuse, normal, material, depth, light diffuse, light specular, ambient occlusion, the cube map, the irradiance map, the blurred dual paraboloid maps, the screen space reflection buffer, and a lookup helper texture are all passed to a large shader that does a lot of math to produce a composited PBR image in linear color space

![enter image description here](https://i.gyazo.com/9e8b94350060a6043c2bb28bf1bc807a.png)

**Sky**

The cloud maps and a night sky texture are used to create a sky. The sky is a tessellated octahedron hemisphere, the atmospheric scattering is calculated per vertex here instead of per fragment to accelerate things.

![enter image description here](https://i.gyazo.com/9bb9e88a82d62414b66ecc0584049e15.png)

**Moon**

The moon is not visible here, but it is rendered using the GL_ONE blend mode using a texture atlas. The text is just a note I left for myself

![enter image description here](https://i.gyazo.com/e24f93d438fe5f13ab8074b06c0380f8.png)

**Light Bulbs**

The different light sources get drawn over the scene as little light bulbs for realism. Instancing is used for acceleration as there are dozens of light sources in frame

![enter image description here](https://i.gyazo.com/ac353472c04fa542add8a9acede3e588.png)
![enter image description here](https://i.gyazo.com/5fb9322107e58c9f44a5d2f7b97a4470.png)

**Light Shafts**

A light shaft map (also called volumetric lighting, or godrays) is generated by raytracing the sun's first and second shadow map

![enter image description here](https://i.gyazo.com/528d5a38413938556ce9c797b8d24a82.png)

**Fog**

The blend mode is set to add and fog is blended over the scene. The light shaft map is used to selectively darken areas of fog for light shaft effects

**Semi transparent geometry**

Some of the spheres in the scene were drawn dithered, in a checkerboard pattern. These checkerboard patterns are now healed using data stored in the diffuse buffer's alpha channel, to produce transparent geometry.

![enter image description here](https://i.gyazo.com/b7fa7b9ff2735e366890f25c15f0c49d.png)
![enter image description here](https://i.gyazo.com/2acb59791febfac300755d776646231d.png)

**Lens Flares**

Lens flares for the sun are now drawn using a single draw call

![enter image description here](https://i.gyazo.com/3588102d40851c0a758a5f88c0585b28.png)
![enter image description here](https://i.gyazo.com/74b212111a0824ea50ec20c0fe2c177e.png)

**Bloom and Exposure Calculation**

The buffer is repeatedly downscaled and blurred in multiple different passes to produce two blurred versions of the scene, one at a quarter resolution and one at an eight with more blur. These are blended over the scene to simulate bloom and reflections within the camera.

**Tonemapping**

Finally, the Uncharted 4 operator is used to convert the linear 16-bit floating point HDR colors to 8-bit sRGB colors. This is done in a single pass.

**Text**

The debug text is drawn using a large unicode texture atlas generated with a class in the tools directory, containing a bitmap of every unicode codepoint. The letters are each drawn as single meshes using premultiplied alpha and instancing.

![enter image description here](https://i.gyazo.com/8a1918615438a06870fa3d0edee16e26.png)

Wireframe:
![enter image description here](https://i.gyazo.com/19a5907ec78e6800cad3252270325a1e.png)

Unicode:
![enter image description here](https://i.gyazo.com/49c876e6226fc85909cac868753fef3c.png)

Texture Atlas:
![enter image description here](https://i.gyazo.com/8ca790be879eb8a016a6bfb2dd41ef86.png)
![enter image description here](https://i.gyazo.com/2cfa673685d7caa424aa7ad7a156cf10.png)

**Completed Frame**

![enter image description here](https://i.gyazo.com/c82623f71b7a449f311b223a18dce50d.png)