const mysql = require('mysql2/promise');

async function initDB() {
  try {
    const connection = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: '123'
    });

    // Create Database
    await connection.execute(`CREATE DATABASE IF NOT EXISTS hiphop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`);
    console.log('Database hiphop created or exists.');

    await connection.changeUser({ database: 'hiphop' });

    // Create Buzz Table
    const createTableSQL = `
      CREATE TABLE IF NOT EXISTS buzz (
        id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        summary TEXT,
        cover_image VARCHAR(512),
        author VARCHAR(100),
        publish_time DATETIME,
        tag VARCHAR(50),
        views INT DEFAULT 0,
        source VARCHAR(50),
        source_url VARCHAR(512),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `;
    await connection.execute(createTableSQL);
    console.log('Table buzz created or exists.');

    await connection.end();
  } catch (err) {
    console.error('Error initializing DB:', err);
  }
}

initDB();
