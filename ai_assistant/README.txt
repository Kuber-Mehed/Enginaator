# To run the microservice:
# 1. Make sure your audio2text.py and text_processing.py scripts are working and accept arguments as expected.
# 2. Update the FORWARD_ENDPOINT in microservice.py to your target endpoint.
# 3. Start the server with:
#    uvicorn ai_assistant.microservice:app --reload
#
# Example curl request:
# curl -X POST "http://127.0.0.1:8000/process-audio/" -F "file=@yourfile.mp3"
