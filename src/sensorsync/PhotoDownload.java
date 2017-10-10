package sensorsync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import common.EncDec;
import common.Tracer;

public class PhotoDownload {
	private static final Logger LOGGER = Tracer.getLogger(PhotoDownload.class);

	private static boolean downloadSuccess = false;

	private static String server;

	private static String user;

	private static String pass;

	private static int port;

	public static void main(String[] args) throws IOException {		
		
		Tracer.setup();
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File("config.sensorsync.properties")));
		setUpProperties(prop);
		
		long init = System.currentTimeMillis();

		FTPClient ftpClient = new FTPClient();

		try {
			// connect and login to the server
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);

			// use local passive mode to pass firewall
			ftpClient.enterLocalPassiveMode();

			System.out.println("Connected");

			String remoteDirPath = "/www/bdncapac/upload";

			String saveDirPath = "photos";

			File f = new File(saveDirPath + remoteDirPath);
			if (!f.exists()) {
				f.mkdirs();
			}

			downloadDirectory(ftpClient, remoteDirPath, "", saveDirPath);

			// log out and disconnect from the server
			ftpClient.logout();
			ftpClient.disconnect();

			System.out.println("Disconnected");

			if (downloadSuccess) {
				LOGGER.log(Level.INFO, "The pictures were downloaded correctly.");
			} else {
				LOGGER.log(Level.SEVERE, "The pictures couldnt be downloaded!");
			}
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Error downloading the pictures: "+ex.toString(),ex);
		}

		System.out.println("Finish " + (System.currentTimeMillis() - init));
	}

	private static void setUpProperties(Properties prop) {	
		server = EncDec.dec(prop.getProperty("ftp_server"));
		port = Integer.parseInt(EncDec.dec(prop.getProperty("ftp_port")));
		user = EncDec.dec(prop.getProperty("ftp_user"));
		pass = EncDec.dec(prop.getProperty("ftp_password"));		
	}

	/**
	 * Download a whole directory from a FTP server.
	 * 
	 * @param ftpClient
	 *            an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param parentDir
	 *            Path of the parent directory of the current directory being
	 *            downloaded.
	 * @param currentDir
	 *            Path of the current directory being downloaded.
	 * @param saveDir
	 *            path of directory where the whole remote directory will be
	 *            downloaded and saved.
	 * @throws IOException
	 *             if any network or IO error occurred.
	 */
	public static void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir)
			throws IOException {
		int successCount = 0;
		int failCount = 0;

		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir;
		}

		FTPFile[] subFiles = ftpClient.listFiles(dirToList);

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
				if (currentDir.equals("")) {
					filePath = parentDir + "/" + currentFileName;
				}

				String newDirPath = saveDir + parentDir + File.separator + currentDir + File.separator
						+ currentFileName;
				if (currentDir.equals("")) {
					newDirPath = saveDir + parentDir + File.separator + currentFileName;
				}

				if (aFile.isDirectory()) {

				} else {
					// download the file
					boolean success = downloadSingleFile(ftpClient, filePath, newDirPath);
					if (success) {
						System.out.println("DOWNLOADED the file: " + filePath);
						successCount++;
					} else {
						System.out.println("COULD NOT download the file: " + filePath);
						failCount++;
					}
				}
			}
		}

		int total = successCount + failCount;
		double percentSuccess = ((double) successCount / (double) total) * 100;

		downloadSuccess = percentSuccess > 80;

		System.out.println("success: " + downloadSuccess);

	}

	/**
	 * Download a single file from the FTP server
	 * 
	 * @param ftpClient
	 *            an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteFilePath
	 *            path of the file on the server
	 * @param savePath
	 *            path of directory where the file will be stored
	 * @return true if the file was downloaded successfully, false otherwise
	 * @throws IOException
	 *             if any network or IO error occurred.
	 */
	public static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath)
			throws IOException {
		File downloadFile = new File(savePath);

		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			return ftpClient.retrieveFile(remoteFilePath, outputStream);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

}
