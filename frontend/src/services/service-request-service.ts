import type {InventoryItem} from "@/utils/types/inventory.ts";
import api from "@/services/api.ts";

export const createServiceRequest = async (roomNumber: string, audioBlob: Blob) => {
    const formData = new FormData()
    formData.append('file', audioBlob, 'guest-request.webm')

    await api.post(`/room/${roomNumber}`, formData)
}

export const postGuestRequestTxt = async (roomNumber: string, requestText: string) => {
    await api.post(`/room/${roomNumber}/text`, {
        requestText,
    })
}