pipeline {
    agent any

    stages {
        // Không viết stage Checkout ở đây nữa!
        
        stage('Build') {
            steps {
                echo 'Đang build ứng dụng...'
                // Thêm lệnh build của bạn vào đây, ví dụ:
                // sh './mvnw clean package' hoặc bat 'mvn clean package'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Đang chạy test...'
            }
        }
    }
}