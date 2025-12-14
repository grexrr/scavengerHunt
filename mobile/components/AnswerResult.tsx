import React from 'react';
import { Modal, Text, TouchableOpacity, View } from 'react-native';
import { resultStyles } from '../styles/resultStyles';

interface AnswerResultProps {
  message?: string;
  visible: boolean;
  onClose: () => void;
}

export default function AnswerResult({ message, visible, onClose }: AnswerResultProps) {
  if (!message) return null;

  return (
    <Modal visible={visible} transparent={true} animationType="fade" onRequestClose={onClose}>
      <View style={resultStyles.modalOverlay}>
        <View style={resultStyles.modalContent}>
          <Text style={resultStyles.modalTitle}>Answer Result</Text>
          <Text style={resultStyles.modalMessage}>{message}</Text>
          <TouchableOpacity style={resultStyles.modalButton} onPress={onClose} activeOpacity={0.8}>
            <Text style={resultStyles.modalButtonText}>OK</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}
