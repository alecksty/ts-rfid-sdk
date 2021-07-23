#
echo "Compiler Native USB & UART Port With Linux API ..."
###########################################################################
export  JniJavaInclude=/usr/lib/jvm/java-8-openjdk-amd64/include
export  JniLinuxInclude=/usr/lib/jvm/java-8-openjdk-amd64/include/linux
#
export  OutputUsbFile=libNativeUsb.so
export  OutputUartFile=libNativeUart.so
#
export  SourceUsbFile=com_tanso_rfidlib_port_usb_NativeUsb.cpp
export  SourceUartFile=com_tanso_rfidlib_port_uart_NativeUart.cpp

###########################################################################
#
###########################################################################
echo  "Include : $JniJavaInclude"
echo  "Include : $JniLinuxInclude"

#
echo "Remove Output Files ..."
rm -v *.o
rm -v *.so
rm -v *.out

###########################################################################
#
###########################################################################
echo "Compiler Stander EXE File..."

gcc     -I$JniJavaInclude \
        -I$JniLinuxInclude \
        $SourceUsbFile \
        -o usb.out

gcc     -I$JniJavaInclude \
        -I$JniLinuxInclude \
        $SourceUartFile \
        -o uart.out
###########################################################################
#
###########################################################################
echo "Compiler SO File..."

gcc     -fPIC \
        -shared \
        -I$JniJavaInclude \
        -I$JniLinuxInclude \
        -c $SourceUsbFile \
        -o libusb.o

gcc -Wl,-soname,$OutputUsbFile -shared libusb.o -o $OutputUsbFile

gcc     -fPIC \
        -shared \
        -I$JniJavaInclude \
        -I$JniLinuxInclude \
        -c $SourceUartFile \
        -o libuart.o

gcc -Wl,-soname,$OutputUartFile -shared libuart.o -o $OutputUartFile

###########################################################################
# Update Java Header Files
###########################################################################
javah -cp .. -v com.tanso.rfidlib.port.usb.NativeUsb
javah -cp .. -v com.tanso.rfidlib.port.uart.NativeUart

###########################################################################
#
###########################################################################
echo "Make $OutputUsbFile done!"
cp $OutputUsbFile /lib
cp $OutputUsbFile /lib64

echo "Make $OutputUartFile done!"
cp $OutputUartFile /lib
cp $OutputUartFile /lib64
