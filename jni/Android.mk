LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ndkhello
LOCAL_SRC_FILES :=ndkhello.c

include $(BUILD_SHARED_LIBRARY)