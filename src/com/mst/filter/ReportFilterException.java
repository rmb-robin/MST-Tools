package com.mst.filter;

public class ReportFilterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7799163867965862671L;

	public ReportFilterException() {
	}

	public ReportFilterException(String message) {
		super(message);
	}

	public ReportFilterException(Throwable cause) {
		super(cause);
	}

	public ReportFilterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportFilterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
