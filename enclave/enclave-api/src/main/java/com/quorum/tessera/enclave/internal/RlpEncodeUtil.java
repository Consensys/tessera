package com.quorum.tessera.enclave.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Liftand contain RLP encoding functions. Introduced to decouple tessera from tuweni which was
 * introduced via orion. Code taken from https://github.com/ZuInnoTe/hadoopcryptoledger due to its
 * not requiring numberous external dependencies.
 */
public class RlpEncodeUtil {

  private RlpEncodeUtil() {}

  public static byte[] encodeList(List<byte[]> rawElementList) {

    byte[] result;
    int totalSize = 0;
    if ((rawElementList == null) || (rawElementList.size() == 0)) {
      return new byte[] {(byte) 0xc0};
    }

    List<byte[]> elementList =
        rawElementList.stream()
            .map(RlpEncodeUtil::encodeRLPElement)
            .collect(Collectors.toUnmodifiableList());

    for (int i = 0; i < elementList.size(); i++) {
      totalSize += elementList.get(i).length;
    }
    int currentPosition = 0;
    if (totalSize <= 55) {
      result = new byte[1 + totalSize];
      result[0] = (byte) (0xc0 + totalSize);
      currentPosition = 1;
    } else {
      ByteBuffer bb = ByteBuffer.allocate(4);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      bb.putInt(totalSize);
      byte[] intArray = bb.array();
      int intSize = 0;
      for (int i = 0; i < intArray.length; i++) {
        if (intArray[i] == 0) {
          break;
        } else {
          intSize++;
        }
      }
      result = new byte[1 + intSize + totalSize];
      result[0] = (byte) (0xf7 + intSize);
      byte[] rawDataNumber = Arrays.copyOfRange(intArray, 0, intSize);

      Collections.reverse(List.of(rawDataNumber));

      for (int i = 0; i < rawDataNumber.length; i++) {

        result[1 + i] = rawDataNumber[i];
      }

      currentPosition = 1 + intSize;
    }
    // copy list items
    for (int i = 0; i < elementList.size(); i++) {
      byte[] currentElement = elementList.get(i);
      for (int j = 0; j < currentElement.length; j++) {
        result[currentPosition] = currentElement[j];
        currentPosition++;
      }
    }
    return result;
  }

  protected static byte[] encodeRLPElement(byte[] rawData) {

    byte[] result = null;
    if ((rawData == null) || (rawData.length == 0)) {
      return new byte[] {(byte) 0x80};
    } else if (rawData.length <= 55) {
      if ((rawData.length == 1) && (((int) rawData[0] & 0xFF) <= 0x7F)) {
        return new byte[] {(byte) (rawData[0])};
      }
      result = new byte[rawData.length + 1];
      result[0] = (byte) (0x80 + rawData.length);
      for (int i = 0; i < rawData.length; i++) {
        result[i + 1] = rawData[i];
      }
    } else {
      ByteBuffer bb = ByteBuffer.allocate(4);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      bb.putInt(rawData.length);
      byte[] intArray = bb.array();
      int intSize = 0;
      for (int i = 0; i < intArray.length; i++) {
        if (intArray[i] == 0) {
          break;
        } else {
          intSize++;
        }
      }
      result = new byte[1 + intSize + rawData.length];
      result[0] = (byte) (0xb7 + intSize);
      byte[] rawDataNumber = Arrays.copyOfRange(intArray, 0, intSize);
      Collections.reverse(List.of(rawDataNumber));

      for (int i = 0; i < rawDataNumber.length; i++) {
        result[1 + i] = rawDataNumber[i];
      }
      for (int i = 0; i < rawData.length; i++) {
        result[1 + intSize + i] = rawData[i];
      }
    }
    return result;
  }
}
