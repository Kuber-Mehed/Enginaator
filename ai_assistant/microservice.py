
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import shutil
import os
from faster_whisper import WhisperModel
import librosa

app = FastAPI(title="Audio2Text + TextProcessing Service")

# --- Text processing model setup ---
model_id = "Qwen/Qwen2.5-1.5B-Instruct"
if torch.cuda.is_available():
    device = "cuda"
elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
    device = "mps"
else:
    device = "cpu"

tokenizer = AutoTokenizer.from_pretrained(model_id)
model = AutoModelForCausalLM.from_pretrained(
    model_id,
    torch_dtype=torch.float16 if device == "cuda" else torch.float32,
    device_map="auto" if device == "cuda" else None,
    trust_remote_code=False
)
if device != "cuda":
    model.to(device)
generator = pipeline(
    "text-generation",
    model=model,
    tokenizer=tokenizer
)

def process_text(text: str):
    messages = [
        {
            "role": "system",
            "content": (
                "You are a professional hotel inventory assistant.\n\n"
                "TASK:\n"
                "1. Extract facility items and their quantities from the user text.\n"
                "2. Translate all item names to English.\n"
                "3. Output the result ONLY as a valid JSON array of objects.\n\n"
                "[\n"
                "  {\"itemName\": \"string\", \"quantity\": integer}\n"
                "]\n\n"
                "CONSTRAINTS:\n"
                "- Output must be valid JSON parsable by Python json.loads(). No conversational filler, no markdown blocks (like ```json), and no explanations.\n"
                "- If an quantity is not specified, use by default number 1.\n"
                "- If no items are found, return [].\n"
                "- Ensure the output is a single, valid JSON string."
            )
        },
        {
            "role": "user",
            "content": f"Extract from this request: {text}"
        }
    ]
    prompt = tokenizer.apply_chat_template(
        messages,
        tokenize=False,
        add_generation_prompt=True
    )
    outputs = generator(
        prompt,
        max_new_tokens=150,
        do_sample=False,
        return_full_text=False
    )
    return outputs[0]["generated_text"].strip()

class TranscriptionRequest(BaseModel):
    text: str


@app.post("/extract")
async def extract(request: TranscriptionRequest):
    if not request.text:
        raise HTTPException(status_code=400, detail="No text provided")
    result = process_text(request.text)
    print({"result": result})
    return {"result": result}


@app.post("/process-audio/")
async def process_audio(file: UploadFile = File(...)):
    temp_audio_path = f"temp_{file.filename}"
    with open(temp_audio_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    try:
        model = WhisperModel("base", device="cpu", compute_type="int8")
        segments, info = model.transcribe(temp_audio_path)
        transcript = "".join(segment.text for segment in segments)
    except Exception as e:
        os.remove(temp_audio_path)
        raise HTTPException(status_code=500, detail=f"audio2text error: {str(e)}")

    os.remove(temp_audio_path)

    try:
        processed_text = process_text(transcript)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"text_processing error: {str(e)}")

    result_json = processed_text
    print(result_json)
    return JSONResponse(content=result_json)
