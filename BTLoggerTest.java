public class BTLoggerTest {
	public static void main(String[] args) throws InterruptedException{
		BTLogger log = BTLogger.getInstance();
		String p1id = "1001";
		String p2id = "1002";
		String p3id = "1004";
		String p4id = "1005";
		String[] neighborList = {p2id, p3id, p4id};
		log.TCPConnectTo(p1id, p2id);
		Thread.sleep(1000);
		log.TCPConnectTo(p3id, p4id);
		Thread.sleep(1000);
		log.TCPConnectFrom(p1id, p2id);
		Thread.sleep(1000);
		log.TCPConnectFrom(p3id, p4id);
		Thread.sleep(1000);
		log.changeOfPrefNeighbors(p1id, neighborList);
		Thread.sleep(1000);
		log.changeOfOUNeighbor(p1id, p2id);
		Thread.sleep(1000);
		log.unchoked(p1id, p2id);
		Thread.sleep(1000);
		log.choked(p1id, p2id);
		Thread.sleep(1000);
		log.receivedHave(p1id, p2id, "4");
		Thread.sleep(1000);
		log.receivedInterested(p1id, p2id);
		Thread.sleep(1000);
		log.receivedNotInterested(p1id, p2id);
		Thread.sleep(1000);
		log.downloadedPiece(p1id, "1", p2id, "72");
		Thread.sleep(1000);
		log.downloadedFile(p1id);
	}
}