package com.vng.datasync.util;

import com.vng.datasync.data.ChatMessage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author thuannv
 * @since 03/08/2017
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    public static <T> boolean isEmpty(Collection<T> c) {
        return null == c || c.isEmpty();
    }

    public static <T> boolean isEmpty(T[] array) {
        return null == array || array.length == 0;
    }

    public static <T> int getSize(T[] a) {
        return isEmpty(a) ? 0 : a.length;
    }

    public static <T> int getSize(Collection<T> c) {
        return isEmpty(c) ? 0 : c.size();
    }

    public static int binaryInsertChatMessage(List<ChatMessage> list, ChatMessage message) {
        if (list == null || message == null) {
            return -1;
        }

        if (list.size() == 0) {
            list.add(message);
            return 0;
        }

        return binaryInsertChatMessage(list, 0, list.size() - 1, message);
    }

    private static int binaryInsertChatMessage(List<ChatMessage> list, int start, int end, ChatMessage message) {
        int insertPosition = -1;
        if (start >= end) {
            // Message with messageId = 0 is newly created by client and allowed to add to list.
            // This value will be updated later when client receives message with the same request id
            // that client sends to server.
            // In case this value is not updated, this means this message fail to send to server,
            // and will only appear in this list but not in local database.
            if (list.get(end).getId() == message.getId() && message.getId() != 0) {
                return insertPosition;
            }

            if (list.get(end).getCreatedTime() > message.getCreatedTime()) {
                list.add(end, message);
                insertPosition = end;
            } else {
                list.add(end + 1, message);
                insertPosition = end + 1;
            }

            return insertPosition;
        }

        int middle = (start + end) / 2;

        if (message.getCreatedTime() > list.get(middle).getCreatedTime()) {
            return binaryInsertChatMessage(list, middle + 1, end, message);
        } else if (message.getCreatedTime() < list.get(middle).getCreatedTime()) {
            return binaryInsertChatMessage(list, start, middle, message);
        } else {
            if (list.get(middle).getMessageId() == message.getMessageId()) {
                return -1;
            }

            insertPosition = middle + 1;

            list.add(insertPosition, message);
            return insertPosition;
        }
    }

    public static <E, T extends List<E>> boolean isListsEqual(T left, T right, Comparator<E> comparator) {
        if (comparator == null) {
            return false;
        }

        if (left == null && right == null) {
            return true;
        }

        if (left == null) {
            return false;
        }

        if (right == null) {
            return false;
        }

        int leftSize = left.size();
        int rightSize = right.size();

        if (leftSize != rightSize) {
            return false;
        }

        for (int i = 0; i < leftSize; i++) {
            if (comparator.compare(left.get(i), right.get(i)) != 0) {
                return false;
            }
        }

        return true;
    }
}