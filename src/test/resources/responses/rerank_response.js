(request) => {
    try {
        const body = JSON.parse(request.body);
        let query = body['rerank_body']['query']
        let documents = body['rerank_body']['docs']

        let returnDocs = {}
        let i = 0
        for (let d in documents) {
            returnDocs[i] = {
                'text': documents[i]['text'],
                'rank': i,
                'doc_id': documents[i]['doc_id'],
                'document_type': 'text'
            };
            i = i +1
        }

        return {
            body: {
                'ranked_results': returnDocs,
                'query': query
            }
        };
    } catch (e) {
        return {body: 'Failed to parse request.'};
    }
}