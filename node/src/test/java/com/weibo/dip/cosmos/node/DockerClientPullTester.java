package com.weibo.dip.cosmos.node;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import java.util.concurrent.TimeUnit;

public class DockerClientPullTester {
  public static void main(String[] args) throws Exception {
    DockerClient client = DockerClientBuilder.getInstance().build();

    client
        .pullImageCmd("registry.api.weibo.com/dippub/hiveapplication")
        .withTag("0.0.1")
        .exec(new PullImageResultCallback())
        .awaitCompletion(600000, TimeUnit.MILLISECONDS);
  }
}
