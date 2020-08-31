/**
 * 
 */
package dbsync.exception;

/**
 * @author 0380009503
 *
 */
public class DBSyncException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 异常状态码
	 */
	private Integer errorCode;

	/**
	 * 异常信息
	 */
	private String errorMessage;

	public DBSyncException(String errorMessage) {
		super("errorMessage: " + errorMessage);
		this.errorMessage = errorMessage;
	}

	public DBSyncException(Integer errorCode, String errorMessage) {
		super("errorCode: " + errorCode + ", errorMessage: " + errorMessage);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
