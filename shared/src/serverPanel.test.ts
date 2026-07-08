import { describe, it, expect } from 'vitest';
import {
  getRemoteBackendLabel,
  getDefaultRemoteConnectPort,
  normalizeRemoteConnectHost,
  resolveDefaultRemoteConnectHost,
  matchesDatabaseSectorLoadFilter,
  formatDatabaseEntityType,
  buildDatabaseEntityListSql,
} from './serverPanel';

describe('remote backend helpers', () => {
  it('labels backends', () => {
    expect(getRemoteBackendLabel('azure-vm')).toBe('Azure VM (SSH)');
    expect(getRemoteBackendLabel('docker')).toBe('Docker Container');
    expect(getRemoteBackendLabel('starmote')).toBe('StarMote');
    expect(getRemoteBackendLabel(undefined)).toBe('StarMote');
  });

  it('defaults connect port by backend', () => {
    expect(getDefaultRemoteConnectPort('azure-vm')).toBe('22');
    expect(getDefaultRemoteConnectPort('docker')).toBe('4242');
  });

  it('normalizes wildcard listen hosts to loopback', () => {
    expect(normalizeRemoteConnectHost('0.0.0.0')).toBe('127.0.0.1');
    expect(normalizeRemoteConnectHost('all')).toBe('127.0.0.1');
    expect(normalizeRemoteConnectHost('example.com')).toBe('example.com');
    expect(normalizeRemoteConnectHost('  ')).toBeUndefined();
  });

  it('resolves connect host preferring the profile host for remote servers', () => {
    expect(resolveDefaultRemoteConnectHost({ id: '1', name: 'r', isRemote: true, serverIp: '10.0.0.5' }, 'all')).toBe('10.0.0.5');
    expect(resolveDefaultRemoteConnectHost({ id: '1', name: 'l' }, '0.0.0.0')).toBe('127.0.0.1');
  });
});

describe('database helpers', () => {
  it('filters by sector-load state', () => {
    expect(matchesDatabaseSectorLoadFilter(true, 'loaded')).toBe(true);
    expect(matchesDatabaseSectorLoadFilter(true, 'unloaded')).toBe(false);
    expect(matchesDatabaseSectorLoadFilter(false, 'all')).toBe(true);
  });

  it('labels entity types', () => {
    expect(formatDatabaseEntityType('5')).toBe('Ship');
    expect(formatDatabaseEntityType('99')).toBe('Type 99');
    expect(formatDatabaseEntityType('Ship')).toBe('Ship');
  });

  it('builds the entity list SQL', () => {
    expect(buildDatabaseEntityListSql()).toContain('FROM ENTITIES e');
  });
});
