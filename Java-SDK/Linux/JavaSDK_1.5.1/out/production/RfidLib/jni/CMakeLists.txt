cmake_minimum_required(VERSION 3.16)
project(jni)

set(CMAKE_CXX_STANDARD 14)

include_directories(.
        "C:\\Users\\aleck\\.jdks\\openjdk-14.0.1\\include"
        "C:\\Users\\aleck\\.jdks\\openjdk-14.0.1\\include\\win32")

add_executable(jni
        linux/types.h
        linux/hid.h
        linux/hiddev.h
        linux/hidraw.h
        sys/ioctl.h
        com_tanso_rfidlib_port_usb_NativeUsb.cpp
        com_tanso_rfidlib_port_usb_NativeUsb.h)
