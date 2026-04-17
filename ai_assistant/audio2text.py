import sys
from faster_whisper import WhisperModel
import librosa

def main():
    if len(sys.argv) < 2:
        print("Usage: python audio2text.py <audio_file>", file=sys.stderr)
        sys.exit(1)

    audio_path = sys.argv[1]
    model = WhisperModel("base", device="cpu", compute_type="int8")
    audio, sr = librosa.load(audio_path, sr=16000)
    segments, info = model.transcribe(audio)
    full_text = "".join(segment.text for segment in segments)

if __name__ == "__main__":
    main()