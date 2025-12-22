const mysql = require('mysql2/promise');

async function checkDatabases() {
  try {
    const connection = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: '123'
    });

    const [rows] = await connection.execute('SHOW DATABASES');
    console.log('Databases:', rows.map(row => row.Database));

    // 尝试寻找 hiphop 相关的数据库
    const targetDb = rows.find(row => row.Database.includes('hiphop'));
    if (targetDb) {
        console.log(`Found target database: ${targetDb.Database}`);
        // 检查表结构
        await connection.changeUser({ database: targetDb.Database });
        const [tables] = await connection.execute('SHOW TABLES');
        console.log('Tables:', tables);
    }

    await connection.end();
  } catch (err) {
    console.error('Error:', err);
  }
}

checkDatabases();
