package kr.youngho.bodyweightcoach

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PermissionsRationaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        layout.addView(TextView(this).apply {
            text = "Health Connect 데이터 사용 안내"
            textSize = 24f
            gravity = Gravity.START
        })
        layout.addView(TextView(this).apply {
            text = """
                맨몸 코치는 Galaxy Watch와 Samsung Health에 기록된 달리기·수영 운동을 Health Connect를 통해 읽습니다.

                읽는 항목
                • 운동 세션의 시작·종료 시각과 종류
                • 운동 거리
                • 운동 중 심박수 요약
                • 총소모 칼로리

                사용 목적
                • 달리기와 수영 기록을 기존 운동일지에 통합
                • 거리, 시간, 평균 페이스, 심박수 추세 표시

                데이터 처리
                • 읽기 전용이며 Health Connect에 기록을 쓰거나 삭제하지 않습니다.
                • GPS 경로는 요청하거나 읽지 않습니다.
                • 데이터는 휴대폰의 앱 저장공간에만 보관되며 외부 서버로 전송하지 않습니다.
                • 사용자는 Health Connect 설정에서 언제든 권한을 철회할 수 있습니다.
            """.trimIndent()
            textSize = 16f
            setPadding(0, pad, 0, 0)
            movementMethod = ScrollingMovementMethod()
        })
        setContentView(layout)
    }
}
