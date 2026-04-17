import api from '@/services/api.ts'
import type {StaffRequest} from "@/views/StaffDashboard.vue";

export const createServiceRequest = async (roomNumber: string, audioBlob: Blob) => {
    const formData = new FormData()
    formData.append('file', audioBlob, 'guest-request.webm')

    await api.post(`/guest/service-request/room/${roomNumber}`, formData)
}

export const getServiceRequests = async (): Promise<StaffRequest[]> => {
    const res = await api.get('/guest/service-request');
    return res.data
}

export const postGuestRequestTxt = async (roomNumber: string, requestText: string) => {
    await api.post(`/guest/service-request/room/${roomNumber}/text`, {
        requestText,
    })
}

export const updateServiceRequest = async (
    requestId: string,
    isApproved: boolean
): Promise<void> => {
    await api.post(`/guest/service-request/${requestId}`, isApproved)
}