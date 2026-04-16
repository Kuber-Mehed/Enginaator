from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse

import subprocess
import shutil
import os
import requests
import sys

app = FastAPI()


FORWARD_ENDPOINT = "http://your-server.com/receive-result" 

@app.post("/process-audio/")
async def process_audio(file: UploadFile = File(...)):
    if not file.filename.endswith('.mp3'):
        raise HTTPException(status_code=400, detail="Only .mp3 files are supported.")

    #
    temp_audio_path = f"temp_{file.filename}"
    with open(temp_audio_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    
    try:
        result = subprocess.run([
            sys.executable, os.path.join("ai_assistant", "audio2text.py"), temp_audio_path
        ], capture_output=True, text=True, check=True)
        transcript = result.stdout.strip()
    except subprocess.CalledProcessError as e:
        os.remove(temp_audio_path)
        raise HTTPException(status_code=500, detail=f"audio2text error: {e.stderr}")

    
    try:
        result = subprocess.run([
            sys.executable, os.path.join("ai_assistant", "text_processing.py"), transcript
        ], capture_output=True, text=True, check=True)
        processed_text = result.stdout.strip()
    except subprocess.CalledProcessError as e:
        os.remove(temp_audio_path)
        raise HTTPException(status_code=500, detail=f"text_processing error: {e.stderr}")

    os.remove(temp_audio_path)

    
    try:
        resp = requests.post(FORWARD_ENDPOINT, json={"result": processed_text})
        resp.raise_for_status()
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Failed to forward result: {str(e)}")

    return JSONResponse(content={"transcript": transcript, "processed": processed_text, "forwarded": True})
