import { z } from 'zod';
import { config as loadDotenv } from 'dotenv';
import path from 'node:path';

loadDotenv();

const EnvSchema = z.object({
  SSP_HOST: z.string().default('0.0.0.0'),
  SSP_PORT: z.coerce.number().int().positive().default(8080),
  SSP_DATA_DIR: z.string().default('./data'),
  SSP_SECRET_KEY: z.string().optional(),
  SSP_ADMIN_USERNAME: z.string().min(1).default('admin'),
  SSP_ADMIN_PASSWORD: z.string().min(1).optional(),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
});

const parsed = EnvSchema.parse(process.env);

export interface AppConfig {
  host: string;
  port: number;
  /** Absolute path to the data directory (SQLite DB, runtime state). */
  dataDir: string;
  /** Absolute path to the panel SQLite database file. */
  dbPath: string;
  secretKey: string | undefined;
  adminUsername: string;
  adminPassword: string | undefined;
  isProduction: boolean;
  isTest: boolean;
  /** Whether session cookies get the Secure flag (production only). */
  cookieSecure: boolean;
}

const dataDir = path.resolve(process.cwd(), parsed.SSP_DATA_DIR);

export const config: AppConfig = {
  host: parsed.SSP_HOST,
  port: parsed.SSP_PORT,
  dataDir,
  dbPath: path.join(dataDir, 'panel.sqlite'),
  secretKey: parsed.SSP_SECRET_KEY,
  adminUsername: parsed.SSP_ADMIN_USERNAME,
  adminPassword: parsed.SSP_ADMIN_PASSWORD,
  isProduction: parsed.NODE_ENV === 'production',
  isTest: parsed.NODE_ENV === 'test',
  cookieSecure: parsed.NODE_ENV === 'production',
};
