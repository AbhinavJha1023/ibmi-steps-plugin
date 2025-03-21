package org.jenkinsci.plugins.ibmisteps.steps;

import com.ibm.as400.access.AS400Message;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import org.jenkinsci.plugins.ibmisteps.Messages;
import org.jenkinsci.plugins.ibmisteps.model.CallResult;
import org.jenkinsci.plugins.ibmisteps.model.IBMi;
import org.jenkinsci.plugins.ibmisteps.model.LoggerWrapper;
import org.jenkinsci.plugins.ibmisteps.steps.abstracts.IBMiStep;
import org.jenkinsci.plugins.ibmisteps.steps.abstracts.IBMiStepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class IBMiCommandStep extends IBMiStep<CallResult> {
	private static final long serialVersionUID = 6443392002952411163L;

	private final String command;
	private boolean failOnError;

	@DataBoundConstructor
	public IBMiCommandStep(final String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	@DataBoundSetter
	public void setFailOnError(final boolean failOnError) {
		this.failOnError = failOnError;
	}

	@Extension
	public static class DescriptorImpl extends IBMiStepDescriptor {
		@Override
		public String getFunctionName() {
			return "ibmiCommand";
		}

		@NonNull
		@Override
		public String getDisplayName() {
			return Messages.IBMICommandStep_description();
		}
	}

	@Override
	protected CallResult runOnIBMi(final StepContext context, final LoggerWrapper logger, final IBMi ibmi)
			throws Exception {
		logger.log(Messages.IBMICommandStep_running(command));

		final CallResult result = ibmi.executeCommand(command);
		final AS400Message lastMessage = result.getLastMessage();
		if (result.isSuccessful()) {
			logger.log(Messages.IBMICommandStep_succeeded(command));
		} else {
			final String error;
			if (lastMessage != null) {
				error = Messages.IBMICommandStep_failed_with_message(command, lastMessage.getID(), lastMessage.getText());
			} else {
				error = Messages.IBMICommandStep_failed(command);
			}
			if (!failOnError) {
				logger.log(error);
				logger.trace(result.getPrettyMessages());
			} else {
				logger.log(result.getPrettyMessages());
				throw new AbortException(error);
			}
		}

		return result;
	}
}
