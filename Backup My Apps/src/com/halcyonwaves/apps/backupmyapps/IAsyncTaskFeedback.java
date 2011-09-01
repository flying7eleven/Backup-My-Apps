package com.halcyonwaves.apps.backupmyapps;

/**
 * Interface which describes the methods which can be called by an
 * asynchronous task.
 * 
 * @author Tim Huetz
 * @since 0.2
 */
public interface IAsyncTaskFeedback {

	/**
	 * This method gets called if the task execution succeeded.
	 * 
	 * @author Tim Huetz
	 * @param sender The caller of the method.
	 * @param data Additional data for the method call.
	 * @since 0.2
	 */
	void taskSuccessfull(Object sender, Object data);
	
	/**
	 * This method gets called if the task execution failed.
	 * 
	 * @author Tim Huetz
	 * @param sender The caller of the method.
	 * @param data Additional data for the method call.
	 * @since 0.2
	 */
	void taskFailed(Object sender, Object data);
}
