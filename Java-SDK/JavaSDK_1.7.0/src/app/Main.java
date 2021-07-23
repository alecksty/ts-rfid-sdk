package app;

import com.tanso.rfidlib.RfidSystem;
import com.tanso.rfidlib.port.PortManager;
import com.tanso.rfidlib.rfid.TagItem;

import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    /**
     * Reader#type=R2000,ch=4
     * USB@sn=xx.xx.xx.xx;
     * UDP@url=192.168.1.10:8000
     * TCP@url=192.168.2.30:4000
     * UART@path=/dev/ttyS4
     * BLE@dev=xx:xx:xx:xx
     *
     * Reader#type=QM100,ch=1
     * Reader#type=R2000,ch=4
     */
    public static RfidSystem rfidSystem;

    /**
     * 转换输出
     *
     * @param obj : 对象
     * @return : 对象文本描述
     */
    public static String GetText(Object obj) {
        if (obj == null) {
            return "(null)";
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return (String) obj.toString();
        }
    }

    /**
     * 启动
     *
     * @param args ： 参数
     */
    public static void main(String[] args) {

        //  网络接口
        //String strPort = "TCP#ip='10.10.1.100',port=4001";
        //String strRfid = "Reader#type='R2000',ch=4";

        //  USB接口
        String strPort = "USB#dev='/dev/usb/hiddev0'";
        String strRfid = "Reader#type='M100',ch=1";

        //  串行接口
        //String strPort = "UART#path='/dev/ttyS0',baud=115200";
        //String strRfid = "Reader#type='R2000',ch=4";

        //  默认延时
        String strDelay = "5000";

        //  使用说明
        System.out.println("==================================================================================");
        System.out.println("=    ");
        System.out.println("= RFID COMMAND LINE TOOLS V1.60 (Java)");
        System.out.println("=    ");
        System.out.println("=                作者:施探宇");
        System.out.println("=                电话:18680399436");
        System.out.println("=                公司:深圳市探索智能科技有限公司");
        System.out.println("=                网站:http://www.ts-rfid.com");
        System.out.println("==================================================================================");
        System.out.println("Usage : java -jar tsrfid.jar [-port <port config>] [-rfid <rfid config>]");
        System.out.println("  Config As Follow:");
        System.out.println("  Port : tcp/udp#ip='xx.xx.xx.xx',port=num");
        System.out.println("         usb#dev='/dev/usb/hiddev0'");
        System.out.println("         uart#path='/dev/ttyS0',baud=115200");
        System.out.println("   --- ");
        System.out.println("  Rfid : reader#type='qm100/r2000/j2000/g2000/slr5300/slr1200',ch=1/4/8/16");
        System.out.println("Exp :");
        System.out.println("\tjava -jar tsrfid.jar -port \"tcp#ip='10.10.1.100',port=4001\" -rfid \"reader#type='qm100',ch=1\"");
        System.out.println("==================================================================================");

        //  参数过滤
        for (int i = 0; i < args.length; i++) {
            //  端口参数
            if (args[i].equals("-port") || args[i].equals("-p")) {
                if (i + 1 < args.length) {
                    strPort = args[i + 1];
                }
            }
            //  标签参数
            else if (args[i].equals("-rfid") || args[i].equals("-r")) {
                if (i + 1 < args.length) {
                    strRfid = args[i + 1];
                }
            }
            //  延时参数
            else if (args[i].equals("-delay") || args[i].equals("-d")) {
                if (i + 1 < args.length) {
                    strDelay = args[i + 1];
                }
            }
        }

        //  状态
        System.out.println("Found Rfid Config : " + strRfid);
        System.out.println("Found Port Config : " + strPort);
        System.out.println("       Delay Time : " + strDelay);

        //  创建对象
        rfidSystem = new RfidSystem(null, strRfid, strPort
                , (reader, type, cmd, param, obj) -> {
            //
            //System.err.println("OnRfid(reader:" + reader + ",type:" + type + ",cmd:" + cmd + ",param:" + SDK.arrayToText(param) + ",obj:" + GetText(obj));
            //
            if (obj instanceof TagItem) {
                List<TagItem> l = rfidSystem.getRfidManager().getList();
                synchronized (l) {
                    //  遍历没有输出的
                    for (TagItem t : l) {
                    //   new tags
                        if (!t.selected) {
                            t.selected = true;
                        //  显示红色的
                            System.err.println("EPC:" + t.getTextEpc()
                                    + ",PC:" + String.format("%04x", 0xffff & t.pc)
                                    + ",CRC:" + String.format("%04x", 0xffff & t.crc)
                                + ",RSSI:" + t.rssi
                                + ",ANT:" + t.ant
                                + ",CNT:" + t.count);
                        }else {
                        //  普通的显示白色
//                        System.out.println(" EPC:" + t.getTextEpc()
//                                + ",PC:" + String.format("%04x", 0xffff & t.pc)
//                                + ",CRC:" + String.format("%04x", 0xffff & t.crc)
//                                + ",RSSI:" + t.rssi
//                                + ",CNT:" + t.count);
                    	}
                    }
                }
            }
        }, (type, even, param, device, message) -> {
            //  端口消息
            System.out.println("OnPort(type:" + type + ",even:" + even + ",param:" + param + ",dev:" + device + ",msg:" + message + ")");
        });

        //  端口管理器
        PortManager portMan = rfidSystem.getPortManager();

        //  等待连接
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //  开始
        rfidSystem.start();

        int sz = 0;
        //  连上，就一直处理
        while (portMan.getPort().isConnected()) {

            //  延时等待读取
            try {
                sleep(1000);
            } catch (InterruptedException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            //  获取标签数量
            int listSz = rfidSystem.getRfidManager().getList().size();
            //  数量改变
            if (sz != listSz) {
                sz = listSz;
                //  输出标签数量
                System.out.println(">>> Rfid Tags : " + sz);
            }
        }

        //  停机
        rfidSystem.stop();

        //rfidSystem.getRfidManager().clear();

        //  断开连接
        if (portMan.getPort().isConnected()) {
            portMan.getPort().disconnect();
        }

        //  释放资源
        rfidSystem.Release();
        rfidSystem = null;

        System.err.println("<完成> - 连接断开,你需要重新启动此程序!");
        //  退出，不驻留。
        //System.exit(0);
    }
}
