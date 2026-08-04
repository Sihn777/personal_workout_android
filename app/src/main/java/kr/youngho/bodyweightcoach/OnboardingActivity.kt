package kr.youngho.bodyweightcoach

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad,pad,pad,pad) }
        layout.addView(TextView(this).apply { text = "맨몸 코치와 Health Connect 연결"; textSize = 24f })
        layout.addView(TextView(this).apply { text = "앱의 일지 탭에서 읽기 권한을 허용하면 Galaxy Watch의 달리기·수영 기록을 가져올 수 있습니다."; textSize = 16f; setPadding(0,pad,0,pad) })
        layout.addView(Button(this).apply { text = "맨몸 코치 열기"; setOnClickListener { startActivity(Intent(this@OnboardingActivity, MainActivity::class.java)); finish() } })
        setContentView(layout)
    }
}
