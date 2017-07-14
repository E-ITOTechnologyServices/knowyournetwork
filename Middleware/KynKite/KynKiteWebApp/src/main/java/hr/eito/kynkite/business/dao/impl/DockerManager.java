
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


package hr.eito.kynkite.business.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;

import hr.eito.kynkite.business.dao.DockerDao;

/**
 * DAO for accessing Docker daemon
 * 
 * @author Hrvoje
 *
 */
@Component
@Profile({"dev","prod"})
public class DockerManager implements DockerDao {
	
	private static final Logger logger = LoggerFactory.getLogger(DockerManager.class);
	
	DockerClient dockerClient = null;
	
	/**
	 * Connecting Docker client to Docker daemon
	 */
	public DockerManager() {
		super();
		try {
			// Create Docker client
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
	                .build();
	        DockerCmdExecFactory dockerCmdExecFactory = new NettyDockerCmdExecFactory();
	        dockerClient = DockerClientBuilder.getInstance(config)
	                  .withDockerCmdExecFactory(dockerCmdExecFactory)
	                  .build();
		} catch (Exception e) {
			logger.error("Exception during creation of Docker client", e);
		}
	}
	
	/**
	 * Creating report container
	 * 
	 * @param containerName name of the container
	 * @param mountPoint location in container to mount data
	 * @param imageName name of the image
	 * @param volumeName name of the volume to bind data to
	 * @param entrypointCommand 
	 * @param startDate
	 * @param endDate
	 * @param maxDestSyn
	 * @param maxDestIpPing
	 * 
	 * @return created container ID
	 * 
	 * @throws Exception
	 */
	@Override
	public void createReportContainer(final String containerName, 
			final String mountPoint, 
			final String imageName, 
			final String volumeName,
			final String entrypointCommand,
			final String startDate,
			final String endDate,
			final String maxDestSyn,
			final String maxDestIpPing,
			final String reportName) throws Exception {
		Volume volume = new Volume(mountPoint);
		CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(imageName)
        		   .withVolumes(volume)
        		   .withBinds(new Bind(volumeName, volume))
        		   .withName(containerName)
        		   .withEntrypoint(entrypointCommand, startDate, endDate, maxDestSyn, maxDestIpPing, reportName);
        
		// Start container life cycle
		this.containerLifeCycle(createContainerCmd);
	}
	
	/**
	 * Complete lifecycle of an temporary container
	 * <p>Creating container based on create command, connecting created container to network,
	 * starting container, stopping container, removing container
	 * 
	 * @param createContainerCommand command to create container
	 * 
	 * @throws Exception
	 */
	private void containerLifeCycle(CreateContainerCmd createContainerCommand) throws Exception {
		// Creating container
		String containerId = this.createContainer(createContainerCommand);
		
		// Connecting container to the network
		this.connectContainerToKYNNetwork(containerId);
		
		// Starting container
		this.startContainer(containerId);
		
		// Stopping container
		this.stopContainer(containerId);
		
		// Removing container
		this.removeContainer(containerId);
	}
	
	/**
	 * Creating Docker container based on CreateContainerCommand
	 * 
	 * @param createContainerCommand command to create container
	 * 
	 * @return created container ID
	 * 
	 * @throws Exception
	 */
	private String createContainer(CreateContainerCmd createContainerCommand) throws Exception {
		CreateContainerResponse container = createContainerCommand.exec();
		
		this.logContainerStatus(container.getId());
		this.containerExecCheck(container.getId());
 
		return container.getId();
	}
	
	/**
	 * Connecting container to KYN network
	 * 
	 * @param containerId ID of the container
	 * 
	 * @throws Exception
	 */
	private void connectContainerToKYNNetwork(final String containerId) throws Exception {
		Network network = this.getKYNNetwork();
 		dockerClient.connectToNetworkCmd().withNetworkId(network.getId()).withContainerId(containerId).exec();
 		this.logContainerStatus(containerId);
 		this.containerExecCheck(containerId);
	}
	
	/**
	 * Start container
	 * 
	 * @param containerId ID of the container
	 * 
	 * @throws Exception
	 */
	private void startContainer(final String containerId) throws Exception {
		dockerClient.startContainerCmd(containerId).exec();
		this.logContainerStatus(containerId);
		this.containerExecCheck(containerId);
	}
	
	/**
	 * Stop container
	 * 
	 * @param containerId ID of the container
	 * 
	 * @throws Exception
	 */
	private void stopContainer(final String containerId) throws Exception {
		dockerClient.stopContainerCmd(containerId).exec();
		this.logContainerStatus(containerId);
		this.containerExecCheck(containerId);
	}
	
	/**
	 * Remove container
	 * 
	 * @param containerId ID of the container
	 * 
	 * @throws Exception
	 */
	private void removeContainer(final String containerId) throws Exception {
		dockerClient.removeContainerCmd(containerId).exec();
	}
	
	/**
	 * Getting the KYN Docker network based on network name.
	 * <p>KYN network name starts with "kyn" and ends with "_default"
	 * 
	 * @return KYN Docker network
	 */
	private Network getKYNNetwork() {
		List<Network> networks = dockerClient.listNetworksCmd().exec();
        for (Network network : networks) {
        	String networkName = network.getName();
            if (StringUtils.startsWith(networkName, "kyn") && 
            		StringUtils.endsWith(networkName, "_default")) {
                return network;
            }
        }
        return null;
	}
	
	/**
	 * Logging container status
	 * 
	 * @param container created and checked
	 */
	private Integer logContainerStatus(final String containerId) {
		InspectContainerResponse inspectContResp = dockerClient.inspectContainerCmd(containerId).exec();
		logger.info(String.format("Container %s status: %s (%d, %d)", 
				inspectContResp.getId(), inspectContResp.getState().getStatus(), inspectContResp.getState().getExitCode(),
				inspectContResp.getState().getPid()));
		return inspectContResp.getState().getExitCode();
	}
	
	/**
	 * Checking the execution inside container.
	 * <p>Invoke exception if exit code differs from 0.
	 * 
	 * @param container created and checked
	 * @throws Exception Forced exception when container exit code unexpected
	 */
	private void containerExecCheck(final String containerId) throws Exception {
		InspectContainerResponse inspectContResp = dockerClient.inspectContainerCmd(containerId).exec();
		if (inspectContResp.getState().getExitCode().intValue()!=0) {
			throw new Exception("Container answered with exit code: " + inspectContResp.getState().getExitCode());
		}
	}
	
}
