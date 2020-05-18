using System.Diagnostics;
using System.IO;
using System.Net;
using System.Text;
using System.Windows;

namespace WpfRfid.Mode
{
    /// <summary>
    /// 输出
    /// </summary>
    public class PostJson
    {

        private const bool DEBUG = SDK.DEBUG;

        private const bool DEBUG_THIS = false;
        
        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 推送数据
        /// </summary>
        /// <param name="url"></param>
        /// <param name="jsonText"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public static string DoPost(string url, string jsonText)
        {
            //  输出
            if (DEBUG_THIS)
            {
                Debug.Print("DoPost(url:{0}{1})\r\n", url, jsonText);
            }

            var request  = (HttpWebRequest) WebRequest.Create(url);
            var byteData = Encoding.UTF8.GetBytes(jsonText);
            var length   = byteData.Length;

            request.Method        = "POST";
            request.ContentType   = "application/json;charset=UTF-8";
            request.ContentLength = length;

            try
            {
                //  输出流
                var writer = request.GetRequestStream();
                //  输出流
                writer.Write(byteData, 0, length);

                writer.Flush();

                writer.Close();

                HttpWebResponse webResponse = (HttpWebResponse)request.GetResponse();

                string responseString = new StreamReader(webResponse.GetResponseStream(), Encoding.GetEncoding("utf-8")).ReadToEnd();

                if (DEBUG_THIS) { 
                    Debug.Print("网络回复:" + responseString);
                }

                return responseString;
            }
            catch (System.OutOfMemoryException e)
            {
                Debug.Print(e.Message);
            }
            catch (IOException e)
            {
                Debug.Print(e.Message);
            }
            catch (System.Net.WebException e)
            {
                Debug.Print(e.Message);
                Debug.Print("url:" + url);
            }
            catch (System.ArgumentException e)
            {
                Debug.Print(e.Message);
            }
            catch (System.Exception e)
            {
                Debug.Print(e.Message);
            }

            return "ERR";
        }

        ///-------------------------------------------------------------------------------------------------------------
        /// <summary>
        /// 推送
        /// </summary>
        /// <param name="url"></param>
        /// <param name="jsonText"></param>
        ///-------------------------------------------------------------------------------------------------------------
        public static void DoPost(System.Uri url, string jsonText)
        {
            DoPost(url.ToString(),jsonText);
        }
    }
}