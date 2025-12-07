Guide on how to set up ai-service with docker

Open docker desktop  

Go to settings

Open AI tab


Select - Enable Docker Model Runner

Select - Enable host-side TCP connection with port 12434 and all Cors allowed

! If you have an Nvidia Graphics card with driver not lower than xxx you can enable it as well - it'll allow model to run on gpu!

This is docker model runner documentation: https://docs.docker.com/ai/model-runner/get-started/

<img width="1918" height="1090" alt="image" src="https://github.com/user-attachments/assets/07884008-002b-4577-9900-95d0af7a2b6e" /> 

In Docker Hub pull llama3.2:3B-Q4_K_M AI model

<img width="1917" height="1072" alt="image" src="https://github.com/user-attachments/assets/fec48671-2e27-413c-87ab-29f70181f88e" />
<img width="1918" height="1078" alt="image" src="https://github.com/user-attachments/assets/7d076ecb-9b00-4cc5-a33c-104d969fb1ea" />

After you pulled model it should appear in Models tab on left side panel

That's it, you can now run ai-service in docker using DMR!
