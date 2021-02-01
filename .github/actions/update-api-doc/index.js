const fs = require('fs');
const path = require('path');
const YAML = require('yaml')

const core = require('@actions/core');
const github = require('@actions/github');
const { Octokit } = require('@octokit/rest');

const latestVersionLabel = 'latest';

const specsSource = core.getInput('specs-source');
const ghPagesBranch = core.getInput('gh-pages-branch');
const devBranch = core.getInput('dev-branch');
const specTargetPrefix = core.getInput('spec-target-prefix');
const versionsFile = core.getInput('versions-file');

const log = (...args) => console.log(...args); // eslint-disable-line no-console

async function main() {
  try {
    const cfg = getConfig();
    log("config", cfg);

    await writeVersionsFile(cfg);
    writeSpecFiles(specsSource, cfg);

  } catch (error) {
    core.setFailed(error.message);
  }
}

async function writeVersionsFile(cfg) {
  if (cfg.spec.isReleaseVersion) {
    const versions = await fetchVersions(versionsFile, github.context.repo);
    const updatedVersions = updateVersions(versions, cfg.spec.version);
    saveVersionsJson(updatedVersions, path.join(ghPagesBranch, versionsFile));
  }
}

function writeSpecFiles(specsSource, cfg){
  //write json files
  const specYamlString = fs.readFileSync(specsSource, 'utf8');
  const spec = YAML.parse(specYamlString);
  if(cfg.spec.isReleaseVersion) {
    spec.info.version = cfg.spec.version;
    saveSpecYaml(spec, cfg.spec.releaseDist);
  }else{
    spec.info.version = `${cfg.spec.version}-${github.context.hash.substring(0,8)}`;
  }
  saveSpecYaml(spec, cfg.spec.latestDist);
}

/**
 * Fetch versions.json from gh-pages content
 */
async function fetchVersions(versionsFile, repo) {
  const octokit = new Octokit();
  const versions = await octokit.repos.getContent({
    repo: repo.repo,
    owner: repo.owner,
    ref: 'refs/heads/gh-pages',
    path: versionsFile,
    headers: {
      accept: "application/vnd.github.v3.raw",
    }
  });

  if(versions.status === 200){
    return JSON.parse(versions.data);
  }

  throw new Error(
      `Current versions retrieval failed with status: ${versions.headers.status}`
  );
}

function updateVersions(versions, specVersion) {
  versions[specVersion] = {
    spec: specVersion,
    source: specVersion,
  };
  versions["stable"] = {
    spec: specVersion,
    source: specVersion,
  };
  return versions;
}

function saveVersionsJson(versions, versionsDist) {
  fs.writeFileSync(versionsDist, JSON.stringify(versions, null, 1));
}

function saveSpecYaml(spec, specDist) {
  fs.writeFileSync(specDist, YAML.stringify(spec));
}

function getConfig() {

  return {
    spec: calculateSpecDetails(specsSource),
    distDir: ghPagesBranch,
  };
}

function calculateSpecDetails(specFile) {

  const specVersion = calculateSpecVersion(github.context.payload.ref);
  const release = isReleaseVersion(specVersion);
  const latestDist = destinationPath(specFile, latestVersionLabel);
  const releaseDist = destinationPath(specFile, specVersion);

  return {
    path: specFile,
    version: specVersion,
    isReleaseVersion: release,
    latestDist: latestDist,
    releaseDist: releaseDist,
  };
}

function calculateSpecVersion(ref) {
  if(ref === `refs/heads/${devBranch}`){
    return latestVersionLabel;
  }else{
    const releasePattern = /^refs\/tags\/(.+)?/;
    let match = ref.match( releasePattern );
    return match != null && match.length >= 2 ? match[1] : latestVersionLabel;
  }
}

function isReleaseVersion(version) {
  return version !== latestVersionLabel;
}

function destinationPath(specFile, version) {
  const extension = path.extname(specFile);
  return path.join(ghPagesBranch, `${specTargetPrefix}.${version}${extension}`);
}

main();
