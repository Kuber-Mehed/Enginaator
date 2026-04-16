import type {InventoryItem} from "@/utils/types/inventory.ts";
import api from "@/services/api.ts";

export const createServiceRequest = async (roomNumber: string, item: Blob) => {
    await api.post('/room/' + roomNumber, item);
}