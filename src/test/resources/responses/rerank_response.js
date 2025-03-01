(config) => {
    try {
        let request = config.request
        const body = JSON.parse(request.body);
        let query = body['rerank_body']['query']
        let documents = body['rerank_body']['docs']

        let returnDocs = {}

        for (let i = 0; i < documents.length; ++i) {
            returnDocs[i] = {
                'text': documents[i]['text'],
                'rank': i,
                'doc_id': documents[i]['doc_id'],
                'document_type': 'text'
            }
        }

        config.logger.info("Returning ", returnDocs)

        return {
            body: {
                'ranked_results': returnDocs,
                'query': query
            }
        };
    } catch (e) {
        return {body: 'Failed to parse request: ' + e.toString() + '.'};
    }
}