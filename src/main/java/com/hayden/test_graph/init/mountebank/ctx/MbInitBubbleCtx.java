package com.hayden.test_graph.init.mountebank.ctx;

import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.mountebank.exec.MbInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ResettableThread
@Component
public interface MbInitBubbleCtx extends InitBubble {
}
