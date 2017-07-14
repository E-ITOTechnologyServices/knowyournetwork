
/*
    Copyright (C) 2017 e-ito Technology Services GmbH
    e-mail: info@e-ito.de
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package hr.eito.model;


public class JsonReturnData<T> {
	
	public static final int STATUS_FORBIDDEN = 2;
	public static final int STATUS_ERROR = 1;
	public static final int STATUS_OK = 0;
	static final String OK = "OK";
	static final String UNKNOWN = "Unknown error";
	static final String UNKNOWN_SECURITY = "Unknown security error";
	
	private int status;
	private String errorMessage;
	private T content;
	
	/**
	 * Default constructor
	 */
	public JsonReturnData(){
		setStatus(STATUS_ERROR);
	}

	/**
	 * Constructor for error
	 */
	public JsonReturnData(final String errorMessage){
		setErrorMessage(errorMessage);
	}

	/**
	 * Constructor for no error. This ensures the object is OK.
	 *
	 * @param content the content to set
	 */
	public JsonReturnData(final T content){
		this.content = content;
		setOK();
	}

	/**
	 * Get the current status.
	 *
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Get the current message.
	 *
	 * @return the message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Set the status of the object. Only valid status codes are allowed. If the code is invalid,
	 * the status gets set to ERROR and the message goes to UNKNOWN.
	 *
	 * @param status the status to try and set
	 */
	public void setStatus(final int status) {
		if (STATUS_OK == status) {
			this.status = STATUS_OK;
			errorMessage = OK;
		} else if (STATUS_FORBIDDEN == status) {
			this.status = STATUS_FORBIDDEN;
			errorMessage = UNKNOWN_SECURITY;
		}  else {
			this.status = STATUS_ERROR;
			errorMessage = UNKNOWN;
		} 
	}

	/**
	 * Set the status to OK.
	 */
	public void setOK() {
		setStatus(STATUS_OK);
	}

	/**
	 * Discover whether the object is in an OK state.
	 *
	 * @return whether the status is set to OK
	 */
	public boolean isOK() {
		return STATUS_OK == status;
	}
	
	/**
	 * Set an security message. This will cause the forbidden code to be set so the object will no longer
	 * be considered OK.
	 *
	 * @param securityMessage the message to set
	 */
	public void setSecurityMessage(final String securityMessage) {
		this.status = STATUS_FORBIDDEN;
		this.errorMessage = securityMessage;
	}

	/**
	 * Set an error message. This will cause the error code to be set so the object will no longer
	 * be considered OK.
	 *
	 * @param errorMessage the message to set
	 */
	public void setErrorMessage(final String errorMessage) {
		this.status = STATUS_ERROR;
		this.errorMessage = errorMessage;
	}

	/**
	 * Set an error from an Exception.
	 *
	 * @param e the exception from which to get the message
	 */
	public void setError(final Exception e) {
		setErrorMessage("Exception: " + e.toString());
	}

	/**
	 * Get the payload of the object.
	 *
	 * @return the contents
	 */
	public T getContent() {
		return content;
	}

	/**
	 * Set the payload of the object. Note, this does not set the status to OK.
	 */
	public void setContent(T content) {
		this.content = content;
	}
}
