import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn

model_id = "Qwen/Qwen2.5-1.5B-Instruct"

if torch.cuda.is_available():
    device = "cuda"
elif torch.backends.mps.is_available():
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

app = FastAPI(title="Hotel Inventory Extractor")

class TranscriptionRequest(BaseModel):
    text: str

def process_text(text: str):
    messages = [
        {
            "role": "system",
            "content": (
                "You are a hotel inventory assistant.\n"
                "Extract facility items and their quantities.\n"
                "Translate everything to English.\n"
                "Return ONLY valid JSON in this exact format:\n"
                "{\"item_name\": amount}\n"
                "Rules:\n"
                "- No explanations\n"
                "- No extra text\n"
                "- No comments\n"
                "- If nothing found, return {}\n"
            )
        },
        {"role": "user", "content": f"Extract from this request: {text}"}
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

@app.post("/extract")
async def extract(request: TranscriptionRequest):
    if not request.text:
        raise HTTPException(status_code=400, detail="No text provided")
    
    result = process_text(request.text)
    return {"result": result}

if __name__ == "__main__":
    # Run the server
    uvicorn.run(app, host="0.0.0.0", port=8000)