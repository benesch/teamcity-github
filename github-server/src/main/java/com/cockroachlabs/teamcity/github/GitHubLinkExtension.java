// Copyright 2018 The Cockroach Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied. See the License for the specific language governing
// permissions and limitations under the License.

package com.cockroachlabs.teamcity.github;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.vcs.VcsRootInstanceEntry;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class GitHubLinkExtension extends SimplePageExtension {
    @NotNull
    private final SBuildServer server;

    @NotNull
    private final Pattern urlPattern = Pattern.compile("github.com[:/](?<org>[^/.]+)/(?<repo>[^/.]+)");

    public GitHubLinkExtension(@NotNull final WebControllerManager manager,
                               @NotNull final PluginDescriptor pluginDescriptor,
                               @NotNull final SBuildServer serverIn) {
        super(manager, PlaceId.BUILD_SUMMARY, pluginDescriptor.getPluginName(),
            pluginDescriptor.getPluginResourcesPath("githubLink.jsp"));
        server = serverIn;
        register();
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model,
                          @NotNull final HttpServletRequest request) {
        final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
        if (build == null) {
            return;
        }

        Branch branch = build.getBranch();
        if (branch == null) {
            return;
        }

        java.util.List<VcsRootInstanceEntry> vcsRoots = build.getVcsRootEntries();
        if (vcsRoots.isEmpty()) {
            return;
        }

        String vcsRootUrl = vcsRoots.get(0).getVcsRoot().getProperty("url");
        if (vcsRootUrl == null) {
            return;
        }

        Matcher urlMatcher = urlPattern.matcher(vcsRootUrl);
        if (!urlMatcher.find()) {
            return;
        }

        model.put("github_pr_url", String.format("https://github.com/%s/%s/pull/%s",
            urlMatcher.group("org"), urlMatcher.group("repo"), branch.getName()));
        model.put("github_pr_number", branch.getName());

        super.fillModel(model, request);
    }

    @Override
    public boolean isAvailable(@NotNull final HttpServletRequest request) {
        final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
        if (build == null) {
            return false;
        }

        Branch branch = build.getBranch();
        if (branch == null) {
            return false;
        }
        if (!branch.getName().matches("\\d+")) {
            return false;
        }

        java.util.List<VcsRootInstanceEntry> vcsRoots = build.getVcsRootEntries();
        if (vcsRoots.isEmpty()) {
            return false;
        }

        String vcsRootUrl = vcsRoots.get(0).getVcsRoot().getProperty("url");
        if (vcsRootUrl == null) {
            return false;
        }

        return urlPattern.matcher(vcsRootUrl).find();
    }
}
