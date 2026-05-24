import { create } from "zustand";

interface ConversationStore {
  selectedId: string | null;
  setSelectedId: (id: string | null) => void;
  isTyping: Record<string, boolean>;
  setTyping: (convId: string, typing: boolean) => void;
}

export const useConversationStore = create<ConversationStore>((set) => ({
  selectedId: null,
  setSelectedId: (id) => set({ selectedId: id }),
  isTyping: {},
  setTyping: (convId, typing) =>
    set((state) => ({
      isTyping: { ...state.isTyping, [convId]: typing },
    })),
}));
