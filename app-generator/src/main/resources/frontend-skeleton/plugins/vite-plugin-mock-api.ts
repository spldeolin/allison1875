import type { Plugin } from 'vite'
import type { IncomingMessage, ServerResponse } from 'http'

function readBody(req: IncomingMessage): Promise<string> {
  return new Promise((resolve) => {
    let body = ''
    req.on('data', chunk => { body += chunk })
    req.on('end', () => resolve(body))
  })
}

const studentNames = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十', '郑一', '冯二', '陈明', '林芳', '黄强', '杨丽', '朱伟', '徐静', '何亮', '高洁', '马超', '罗斌']
const grades = ['grade1', 'grade2', 'grade3']
const hobbiesPool = ['sports', 'music', 'reading', 'coding']

const studentMockData = studentNames.map((name, i) => {
  const enrollDate = new Date(2024, 8, 1 + (i % 28))
  const loginDate = new Date(2025, 4, 1 + (i % 28), 8 + (i % 12), i * 3 % 60)
  const checkInMs = ((7 + (i % 3)) * 3600 + (i * 7 % 60) * 60) * 1000
  return {
    id: String(i + 1),
    studentName: name,
    studentId: `2024${String(i + 1).padStart(4, '0')}`,
    remark: i % 3 === 0 ? `${name}的备注信息` : '',
    email: `student${i + 1}@example.com`,
    age: 18 + (i % 5),
    gpa: +(2.5 + Math.random() * 1.5).toFixed(2),
    grade: grades[i % 3],
    hobbies: hobbiesPool.slice(0, 1 + (i % 4)),
    enrollmentDate: enrollDate.getTime(),
    lastLoginTime: loginDate.getTime(),
    dailyCheckInTime: checkInMs,
    isActive: i % 5 !== 0,
    idCardNumber: `3101${String(2000 + i).slice(-4)}19${98 + (i % 5)}0${1 + (i % 9)}1${5 + (i % 5)}001${i % 10}`,
    emergencyContact: i % 2 === 0 ? `${name}家长` : ''
  }
})

export default function mockApiPlugin(): Plugin {
  return {
    name: 'vite-plugin-mock-api',
    configureServer(server) {
      server.middlewares.use(async (req: IncomingMessage, res: ServerResponse, next) => {
        if (req.url === '/api/v1/login' && req.method === 'POST') {
          await readBody(req)
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify({
            code: 200,
            msg: 'ok',
            result: {
              token: 'mock-token-' + Date.now(),
              userInfo: {
                id: '1',
                username: 'admin',
                displayName: '管理员'
              },
              permissions: ['StudentBasicInfo', 'CourseSchedule']
            }
          }))
          return
        }

        if (req.url === '/api/v1/studentBasicInfo/list' && req.method === 'POST') {
          const body = JSON.parse(await readBody(req) || '{}')
          const pageNum = body.pageNum || 1
          const pageSize = body.pageSize || 10
          const start = (pageNum - 1) * pageSize
          const list = studentMockData.slice(start, start + pageSize)
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify({
            code: 200,
            msg: 'ok',
            result: {
              list,
              count: studentMockData.length
            }
          }))
          return
        }

        next()
      })
    }
  }
}
