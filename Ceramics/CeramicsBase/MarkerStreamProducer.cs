using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Windows.Forms;
using System.Threading;
using System.Collections;

namespace Ceramics
{
    class MakerStreamProducer
    {
        private Socket m_socListener;
        private Socket m_socWorker;
        private int m_port;
        private ManualResetEvent resetEvent;

        public MakerStreamProducer(int port)
        {
            this.m_port = port;
            resetEvent = new ManualResetEvent(true);
        }

        //Connect Sockets
        public void Start()
        {
            try
            {
                //create the listening socket...
                m_socListener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                IPEndPoint ipLocal = new IPEndPoint(IPAddress.Any, m_port);
                //bind to local IP Address...
                m_socListener.Bind(ipLocal);
                //start listening...
                m_socListener.Listen(4);
                // create the call back for any client connections...
                this.ListenForClient();
            }
            catch (SocketException se)
            {
                MessageBox.Show(se.Message);
            }
        }

        private void ListenForClient()
        {
            if (m_socListener != null)
            {
                try
                {
                    // create the call back for any client connections...
                    m_socListener.BeginAccept(new AsyncCallback(OnClientConnect), null);
                }
                catch (SocketException se)
                {
                    MessageBox.Show(se.Message);
                }
            }
        }

        private void OnClientConnect(IAsyncResult asyn)
        {
            try
            {
                m_socWorker = m_socListener.EndAccept(asyn);
            }
            catch (ObjectDisposedException)
            {
                MessageBox.Show("OnClientConnection: Socket has been closed\n");
            }
            catch (SocketException se)
            {
                MessageBox.Show(se.Message);
            }
        }

        
        //Disconnet sockets.
        //Synchronous disconnect.
        public void Stop()
        {
            CloseClientSocket();
            CloseListenerSocket();
        }

        private void CloseClientSocket()
        {
            if (m_socWorker != null)
            {
                if (m_socWorker.Connected)
                {
                    m_socWorker.Shutdown(SocketShutdown.Both);
                    m_socWorker.Disconnect(true);
                    m_socWorker.Close();
                }
                m_socWorker = null;
            }
        }

        private void CloseListenerSocket()
        {
            if (m_socListener != null)
            {
                if (m_socListener.Connected)
                {
                    m_socListener.Shutdown(SocketShutdown.Both);
                    m_socListener.Disconnect(true);
                    m_socListener.Close();
                }
                m_socListener = null;
            }
        }

        //Send data
        public void SendData(String data)
        {
            
            if ((m_socWorker != null) && m_socWorker.Connected)
            {
                
                try
                {
                    byte[] byteData = System.Text.Encoding.ASCII.GetBytes(data.ToString());
                    int bytesSent = m_socWorker.Send(byteData);
                }
                catch (SocketException se)
                {
                    if (se.ErrorCode != 10035)
                        MessageBox.Show("Send data :" + se.Message);
                }
            }
            else if (m_socWorker == null)
            {
                    this.CloseClientSocket();
                    this.ListenForClient();
            }
        }
   }
}


