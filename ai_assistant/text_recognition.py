from faster_whisper import WhisperModel
import librosa
import os

audio_path = r"C:\Users\Dell\Desktop\Enginaator\ai_assistant\sample.wav"
output_path = os.path.join(os.path.dirname(audio_path), "transcription.txt")


model = WhisperModel("base", device="cpu", compute_type="int8")

audio, sr = librosa.load(audio_path, sr=16000)


segments, info = model.transcribe(audio)

full_text = ""

for segment in segments:
    full_text += segment.text + "\n"

with open(output_path, "w", encoding="utf-8") as f:
    f.write(full_text)

print(f"Transcription saved to: {output_path}")