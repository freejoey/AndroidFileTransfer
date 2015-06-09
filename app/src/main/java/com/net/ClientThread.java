package com.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.Constants;
import com.Tools;
import com.net.SendFileThread.OnThreadListener;
import com.ui.NetBroadcastReceiver;

public class ClientThread extends Thread {
	private final String Tag = "ClientThread";
	private boolean isRun = false;
	private Socket socket;
	private BufferedInputStream inSocket;
	private BufferedOutputStream outSocket;
	private BufferedOutputStream outFile;
	boolean isRcv = false;// 接收方是否接收
	private SendFileThread senderFileThread;
	private int sendRsp = -1;

	// 10s 内没有数据到来就断开链接
	private final int RCV_TIME_CONTENT = 10000;

	private Context mContext;

	public ClientThread(Socket socket, Context context) throws IOException {
		this.socket = socket;
		this.mContext = context;

		this.inSocket = new BufferedInputStream(this.socket.getInputStream());
		this.outSocket = new BufferedOutputStream(this.socket.getOutputStream());
	}

	public boolean isRunning() {
		return this.isRun;
	}

	public void stopThread() {
		this.interrupt();
		isRun = false;

		try {
			if (this.socket != null)
				socket.close();
			if (inSocket != null)
				inSocket.close();
			if (outSocket != null)
				outSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 通知一个链接断开
		Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
		intent.putExtra("type", NetBroadcastReceiver.FLAG_LOSE_CONN);
		mContext.sendBroadcast(intent);// 传递过去
	}

	public Boolean isServerConnected() {
		try {
			socket.sendUrgentData(0);// 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
			return true;
		} catch (Exception se) {
			return false;
		}
	}

	/*
	 * 发送数据方法
	 */

	public void sendType(int type) {
		if (outSocket != null) {
			try {
				outSocket.write(type);
				outSocket.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int sendFile(final String fp) {
		sendRsp = 2;// 返回状态发送中
		if (isRun == false)
			return -3;// socket closed
		if (!isServerConnected())
			return -4;

		try {
			senderFileThread = new SendFileThread(socket, fp);
			senderFileThread.setListener(new OnThreadListener() {

				@Override
				public void onReturn(int errCode) {
					// TODO Auto-generated method stub
					sendRsp = errCode;

					// 通知发送失败
					Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
					intent.putExtra("type",
							NetBroadcastReceiver.FLAG_TARGET_REFUSE);
					intent.putExtra("addr", socket.getInetAddress()
							.getHostAddress());
					intent.putExtra("file", fp);
					intent.putExtra("errCode", errCode);
					mContext.sendBroadcast(intent);// 传递过去
				}

				@Override
				public void onFinished() {
					// TODO Auto-generated method stub
					sendRsp = 0;

					// 通知发送成功
					Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
					intent.putExtra("type", NetBroadcastReceiver.FLAG_SEND_OK);
					intent.putExtra("addr", socket.getInetAddress()
							.getHostAddress());
					intent.putExtra("file", fp);
					mContext.sendBroadcast(intent);// 传递过去
				}

				@Override
				public void onRefuse() {
					// TODO Auto-generated method stub

					// 通知对方拒绝接收
					Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
					intent.putExtra("type",
							NetBroadcastReceiver.FLAG_TARGET_REFUSE);
					intent.putExtra("addr", socket.getInetAddress()
							.getHostAddress());
					intent.putExtra("file", fp);
					mContext.sendBroadcast(intent);// 传递过去
				}
			});
			senderFileThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sendRsp;
	}

	public void sendRefuse(boolean isRecv) {
		sendType(Constants.NET_TYEP_RCV_RSP);

		int refuseData = Constants.FLAG_REFUSE_RECV;
		if (isRecv)
			refuseData = Constants.FLAG_RECV_FILE;
		if (outSocket != null) {
			try {
				outSocket.write(refuseData);
				outSocket.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 等待对方确认接收文件
	public void waitConfirm() {

	}

	// 是否接收别人发来的文件
	private void askWhetherRecv(String fileName) {
		Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
		intent.putExtra("type", NetBroadcastReceiver.FLAG_IS_RECV);
		intent.putExtra("addr", socket.getInetAddress().getHostAddress());
		intent.putExtra("file", fileName);
		mContext.sendBroadcast(intent);// 传递过去
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyRecv(boolean isRcv) {
		this.isRcv = isRcv;
		synchronized (this) {
			this.notify();
		}
	}

	public void run() {
		isRun = true;
		boolean isReady = true;

		// 这里完成接收数据工作
		while (isRun) {
			// 接收文件准备工作都完成了：isReady = true
			isReady = true;
			isRcv = false;
			// 预计接收长度
			long fileLen = 0;
			// 最终接收数目
			long rcvLen = 0;
			String fileName = null;

			// 首先判断读取数据类型
			int type = -1;
			try {
				type = inSocket.read();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// 1---->对方发送文件
			if (type == Constants.NET_TYEP_FILE_HEADER) {
				// 先接收文件名 和 文件长度
				// 格式：|name|length
				String dataHead = null;
				byte[] bytesHead = new byte[Constants.HEAD_SIZE];
				try {
					int r = -1;
					int j = 0;
					while (j < bytesHead.length) {
						if ((r = inSocket.read()) != -1) {
							bytesHead[j] = (byte) r;
							j++;
						}
					}

					if (r == -1) {
						// 通知断开链接
						Log.i(Tag, "链接断开");
						if (isRunning())
							stopThread();
						break;
					}

					if (j > 0) {
						byte[] real = new byte[j];
						for (int i = 0; i < j; i++) {
							real[i] = bytesHead[i];
						}
						dataHead = new String(real);
						Log.i(Tag, "文件头信息:" + dataHead);
						if (dataHead != null) {
							String splitData[] = dataHead.split("\\|");
							if (splitData.length > 2) {
								Log.i(Tag, "splitData[0]:" + splitData[0]
										+ ", splitData[1]:" + splitData[1]
										+ ", splitData[2]:" + splitData[2]);
								fileName = splitData[1];
								fileLen = Long.parseLong(splitData[2]);
								Log.i(Tag, "文件名:" + fileName + ", 大小:"
										+ fileLen);
							}
							// 获取头部错误
							else {
								Log.i(Tag, "获取头部信息不全, splitData's length："
										+ splitData.length);
								isReady = false;
								if (isRunning())
									stopThread();
							}
						}
					} else {
						// 通知接收头部出错
						isReady = false;
					}

					// 等待确认是否接收*************
					Log.i(Tag, "等待确认接收......");
					askWhetherRecv(fileName);
					Log.i(Tag, "是否接收:" + isRcv);
					sendRefuse(isRcv);

					if (isRcv) {
						File rcvDic = new File(Constants.getAppPath());
						if (!rcvDic.exists()) {
							try {
								boolean re = rcvDic.mkdirs();
								Log.i(Tag, "创建目录：" + rcvDic.getPath()
										+ ", --->" + re);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						String fp = Constants.getAppPath() + "/" + fileName;
						fp = Tools.checkFileExist(fp, 0);
						File newFile = new File(fp);
						if (newFile.createNewFile()) {
							outFile = new BufferedOutputStream(
									new FileOutputStream(newFile));
						} else {
							Log.i(Tag, "创建文件失败");
							isReady = false;
						}

						// 是否创建打开本地文件
						if (outFile == null) {
							// 通知创建本地文件出错......
							Log.i(Tag, "创建本地文件出错");
							isReady = false;
						}

						// 通知要接收文件长度出错......
						if (fileLen <= 0) {
							Log.i(Tag, "接收文件长度出错<= 0");
							isReady = false;
							if (isRunning())
								stopThread();
						}

						if (isReady) {
							rcvLen = 0;
							// 读取长度为fileLen文件,开启计时器
							socket.setSoTimeout(RCV_TIME_CONTENT);
							int rInt = 0;
							for (; rcvLen < fileLen; rcvLen++) {
								rInt = inSocket.read();

								if (rInt != -1) {
									outFile.write(rInt);
								} else {
									Log.i(Tag, "结束标志位:" + rInt
											+ ", 此时接收长度(包含-1):" + rcvLen);
									break;
								}
							}
							socket.setSoTimeout(0);
							Log.i(Tag, "接收字节数 :" + rcvLen);

							if (rcvLen == fileLen) {
								// 通知接收完成......
								Log.i(Tag, "接收完成 :" + fileName);

								Intent intent = new Intent(
										Constants.NET_BROADCAST_FILTER);
								intent.putExtra("type",
										NetBroadcastReceiver.FLAG_NEW_RCV_FILE);
								mContext.sendBroadcast(intent);// 传递过去
							} else {
								// 通知接收不完全......
								Log.i(Tag, "接收不完全,发送方文件长度有误");
							}
						}// isReady
					} 
					else if (!isRcv) {
						// 结束
					}
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					try {
						socket.setSoTimeout(0);
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Log.i(Tag, "接收字节数 :" + rcvLen);
					Log.i(Tag, "接收不完全,接收文件超时");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					stopThread();
				} finally {
					try {
						if (outFile != null) {
							outFile.flush();
							outFile.close();
							outFile = null;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			// 2--->对方是否接收文件回复
			else if (type == Constants.NET_TYEP_RCV_RSP) {
				int rsp = -1;
				try {
					rsp = inSocket.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (rsp == Constants.FLAG_RECV_FILE) {
					if (senderFileThread != null) {
						senderFileThread.setAgreeAble(true);
						synchronized (senderFileThread) {
							senderFileThread.notify();
						}
					}
				} else if (rsp == Constants.FLAG_REFUSE_RECV) {
					Log.i(Tag, "对方拒绝接收文件");
					senderFileThread.setAgreeAble(false);
					synchronized (senderFileThread) {
						senderFileThread.notify();
					}

					Intent intent = new Intent(Constants.NET_BROADCAST_FILTER);
					intent.putExtra("type",
							NetBroadcastReceiver.FLAG_TARGET_REFUSE);
					intent.putExtra("addr", socket.getInetAddress()
							.getHostAddress());
					intent.putExtra("file", fileName);
					mContext.sendBroadcast(intent);// 传递过去
				}
			}
		}// while(isRun)
	}
}
